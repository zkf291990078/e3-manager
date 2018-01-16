package cn.e3mall.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.e3mall.common.pojo.EasyUITreeNode;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.content.service.ContentCategoryService;
import cn.e3mall.mapper.TbContentCategoryMapper;
import cn.e3mall.pojo.TbContentCategory;
import cn.e3mall.pojo.TbContentCategoryExample;
import cn.e3mall.pojo.TbContentCategoryExample.Criteria;

@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

	@Autowired
	private TbContentCategoryMapper contentCategoryMapper;

	@Override
	public List<EasyUITreeNode> getContentCategoryList(long parentId) {
		// TODO Auto-generated method stub
		TbContentCategoryExample example = new TbContentCategoryExample();
		Criteria criteria = example.createCriteria();
		criteria.andParentIdEqualTo(parentId);
		List<TbContentCategory> categories = contentCategoryMapper.selectByExample(example);
		List<EasyUITreeNode> nodes = new ArrayList<>();
		for (TbContentCategory category : categories) {
			EasyUITreeNode node = new EasyUITreeNode();
			node.setId(category.getId());
			node.setText(category.getName());
			node.setState(category.getIsParent() ? "closed" : "open");
			nodes.add(node);
		}
		return nodes;
	}

	@Override
	public E3Result addContentCategory(long parentId, String name) {
		// 1、接收两个参数：parentId、name
		// 2、向tb_content_category表中插入数据。
		// a)创建一个TbContentCategory对象
		TbContentCategory tbContentCategory = new TbContentCategory();
		// b)补全TbContentCategory对象的属性
		tbContentCategory.setIsParent(false);
		tbContentCategory.setName(name);
		tbContentCategory.setParentId(parentId);
		// 排列序号，表示同级类目的展现次序，如数值相等则按名称次序排列。取值范围:大于零的整数
		tbContentCategory.setSortOrder(1);
		// 状态。可选值:1(正常),2(删除)
		tbContentCategory.setStatus(1);
		Date date = new Date();
		tbContentCategory.setCreated(date);
		tbContentCategory.setUpdated(date);
		// c)向tb_content_category表中插入数据
		contentCategoryMapper.insert(tbContentCategory);
		// 3、判断父节点的isparent是否为true，不是true需要改为true。
		TbContentCategory parentNode = contentCategoryMapper.selectByPrimaryKey(parentId);
		if (!parentNode.getIsParent()) {
			parentNode.setIsParent(true);
			// 更新父节点
			contentCategoryMapper.updateByPrimaryKey(parentNode);
		}
		// 4、需要主键返回。
		// 5、返回E3Result，其中包装TbContentCategory对象
		return E3Result.ok(tbContentCategory);
	}

	@Override
	public E3Result updateContentCategory(Long id, String name) {
		// TODO Auto-generated method stub
		TbContentCategory category= contentCategoryMapper.selectByPrimaryKey(id);
		category.setName(name);
		category.setUpdated(new Date());
		contentCategoryMapper.updateByPrimaryKeySelective(category);
		return E3Result.ok();
	}

	@Override
	public E3Result deleteContentCategory(Long parentId, Long id) {
		// TODO Auto-generated method stub
		TbContentCategory currentCategory=contentCategoryMapper.selectByPrimaryKey(id);
		if(currentCategory.getIsParent()){
			deleteCategory(id);
		}
		contentCategoryMapper.deleteByPrimaryKey(id);
		
		TbContentCategoryExample example=new TbContentCategoryExample();
		example.createCriteria().andParentIdEqualTo(parentId);
		List<TbContentCategory> childCates=  contentCategoryMapper.selectByExample(example);
		if(childCates==null||childCates.size()==0){
			TbContentCategory category= contentCategoryMapper.selectByPrimaryKey(parentId);
			category.setIsParent(false);
			contentCategoryMapper.updateByPrimaryKey(category);
		}
		return E3Result.ok();
	}

	private void deleteCategory(Long id){
		TbContentCategoryExample categoryExample=new TbContentCategoryExample();
		categoryExample.createCriteria().andParentIdEqualTo(id);
		List<TbContentCategory> categories=contentCategoryMapper.selectByExample(categoryExample);
		for(TbContentCategory c:categories){
			if(c.getIsParent()){
				deleteCategory(c.getId());
			}else{
				contentCategoryMapper.deleteByPrimaryKey(c.getId());
			}
		}
		
	}
	
}
