package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mysql.fabric.hibernate.FabricMultiTenantConnectionProvider;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.mapper.TbItemCatMapper;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
import cn.e3mall.pojo.TbItemCat;
import cn.e3mall.pojo.TbItemDesc;
import cn.e3mall.pojo.TbItemExample;
import cn.e3mall.service.ItemService;

/**
 * 商品管理Service
 * <p>
 * Title: ItemServiceImpl
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Company: www.itcast.cn
 * </p>
 * 
 * @version 1.0
 */
@Service
public class ItemServiceImpl implements ItemService {

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbItemDescMapper itemDescMapper;
	// @Autowired
	// private TbItemCatMapper itemCatMapper;

	@Override
	public TbItem getItemById(long id) {
		TbItem item = itemMapper.selectByPrimaryKey(id);
		return item;
	}

	@Override
	public EasyUIDataGridResult getItemList(int page, int rows) {

		// 设置分页信息
		PageHelper.startPage(page, rows);
		// 执行查询
		TbItemExample example = new TbItemExample();
		List<TbItem> list = itemMapper.selectByExample(example);
		// for (TbItem item : list) {
		// TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(item.getCid());
		// item.setCname(itemCat.getName());
		// }

		// 取分页信息
		PageInfo<TbItem> pageInfo = new PageInfo<>(list);

		// 创建返回结果对象
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(list);

		return result;
	}

	@Override
	public E3Result addItem(TbItem item, String desc) {
		// TODO Auto-generated method stub
		long itemId = IDUtils.genItemId();
		item.setId(itemId);
		Date date = new Date();
		item.setCreated(date);
		item.setUpdated(date);
		item.setStatus((byte) 1);
		itemMapper.insert(item);
		TbItemDesc itemDesc = new TbItemDesc();
		itemDesc.setItemId(itemId);
		itemDesc.setCreated(date);
		itemDesc.setItemDesc(desc);
		itemDesc.setUpdated(date);
		itemDescMapper.insert(itemDesc);
		return E3Result.ok();
	}

	@Override
	public E3Result showItemDesc(long itemId) {
		TbItemDesc desc = itemDescMapper.selectByPrimaryKey(itemId);
		return E3Result.ok(desc);
	}

	@Override
	public E3Result updateItem(TbItem item, String desc) {
		// TODO Auto-generated method stub.
		Date date = new Date();
		item.setUpdated(date);
		item.setStatus((byte) 1);
		itemMapper.updateByPrimaryKeySelective(item);
		TbItemDesc itemDesc = new TbItemDesc();
		itemDesc.setItemDesc(desc);
		itemDesc.setUpdated(date);
		itemDesc.setItemId(item.getId());
		itemDescMapper.updateByPrimaryKeySelective(itemDesc);
		return E3Result.ok();
	}

	@Override
	public E3Result instocjItem(Long[] ids) {
		// TODO Auto-generated method stub
		for (Long id : ids) {
			TbItem item = itemMapper.selectByPrimaryKey(id);
			// 商品状态，1-正常，2-下架，3-删除
			item.setStatus((byte) 2);
			itemMapper.updateByPrimaryKeySelective(item);
		}
		return E3Result.ok();
	}

	@Override
	public E3Result reshelfItem(Long[] ids) {
		// TODO Auto-generated method stub
		for (Long id : ids) {
			TbItem item = itemMapper.selectByPrimaryKey(id);
			// 商品状态，1-正常，2-下架，3-删除
			item.setStatus((byte) 1);
			itemMapper.updateByPrimaryKeySelective(item);
		}
		return E3Result.ok();
	}

	@Override
	public E3Result deleteItem(Long[] ids) {
		// TODO Auto-generated method stub
		for (Long id : ids) {
			TbItem item = itemMapper.selectByPrimaryKey(id);
			// 商品状态，1-正常，2-下架，3-删除
			item.setStatus((byte) 3);
			itemMapper.updateByPrimaryKeySelective(item);
		}
		return E3Result.ok();
	}
}
