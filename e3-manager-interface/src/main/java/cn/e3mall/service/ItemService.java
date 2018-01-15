package cn.e3mall.service;

import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.pojo.TbItem;

public interface ItemService {

	TbItem getItemById(long id);

	public EasyUIDataGridResult getItemList(int page, int rows);

	public E3Result addItem(TbItem item, String desc);

	public E3Result showItemDesc(long itemId);

	public E3Result updateItem(TbItem item, String desc);

	public E3Result instocjItem(Long[] ids);

	public E3Result deleteItem(Long[] ids);

	public E3Result reshelfItem(Long[] ids);
}
