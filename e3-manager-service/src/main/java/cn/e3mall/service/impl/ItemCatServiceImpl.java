package cn.e3mall.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.mapper.TbItemCatMapper;
import cn.e3mall.pojo.TbItemCat;
import cn.e3mall.pojo.TbItemCatExample;
import cn.e3mall.pojo.TbItemCatExample.Criteria;
import cn.e3mall.service.ItemCatService;

@Service
public class ItemCatServiceImpl implements ItemCatService {

	@Autowired
	TbItemCatMapper itemCatMapper;

	@Override
	public List<EasyUITreeNode> getSelectTreeNodes(long patentId) {
		// TODO Auto-generated method stub
		TbItemCatExample example = new TbItemCatExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(patentId);
		List<TbItemCat> itemCats = itemCatMapper.selectByExample(example);
		List<EasyUITreeNode> list = new ArrayList<>();
		for (TbItemCat cat : itemCats) {
			EasyUITreeNode node = new EasyUITreeNode();
			node.setId(cat.getId());
			node.setText(cat.getName());
			node.setState(cat.getIsParent() ? "closed" : "open");
			list.add(node);
		}
		return list;
	}

}
