package cn.e3mall.service.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import cn.e3mall.common.jedis.JedisClient;
import cn.e3mall.common.pojo.EasyUIDataGridResult;
import cn.e3mall.common.utils.E3Result;
import cn.e3mall.common.utils.IDUtils;
import cn.e3mall.common.utils.JsonUtils;
import cn.e3mall.mapper.TbItemDescMapper;
import cn.e3mall.mapper.TbItemMapper;
import cn.e3mall.pojo.TbItem;
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
	@Autowired
	private JmsTemplate jmsTemplate;
	@Resource
	private Destination topicDestination;
	@Autowired
	private JedisClient jedisClient;
	@Value("${ITEM_INFO_PRE}")
	private String ITEM_INFO_PRE;
	@Value("${ITEM_INFO_EXPIRE}")
	private int ITEM_INFO_EXPIRE;

	@Override
	public TbItem getItemById(long id) {
		String json = jedisClient.get(ITEM_INFO_PRE + ":" + id + ":BASE");
		if (StringUtils.isNotBlank(json)) {
			// 把json转换为java对象
			TbItem item = JsonUtils.jsonToPojo(json, TbItem.class);
			return item;
		}
		TbItem item = itemMapper.selectByPrimaryKey(id);
		try {
			// 把数据保存到缓存
			jedisClient.set(ITEM_INFO_PRE + ":" + id + ":BASE", JsonUtils.objectToJson(item));
			// 设置缓存的有效期
			jedisClient.expire(ITEM_INFO_PRE + ":" + id + ":BASE", ITEM_INFO_EXPIRE);
		} catch (Exception e) {
			e.printStackTrace();
		}

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
		final long itemId = IDUtils.genItemId();
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
		jmsTemplate.send(topicDestination, new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
				// TODO Auto-generated method stub
				TextMessage textMessage = session.createTextMessage(itemId + "");
				return textMessage;
			}
		});
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

	@Override
	public TbItemDesc getItemDescById(long itemid) {
		// TODO Auto-generated method stub
		String json = jedisClient.get(ITEM_INFO_PRE + ":" + itemid + ":DESC");
		if (StringUtils.isNotBlank(json)) {
			// 把json转换为java对象
			TbItemDesc itemDesc = JsonUtils.jsonToPojo(json, TbItemDesc.class);
			return itemDesc;
		}
		TbItemDesc itemDesc = itemDescMapper.selectByPrimaryKey(itemid);
		try {
			// 把数据保存到缓存
			jedisClient.set(ITEM_INFO_PRE + ":" + itemid + ":DESC", JsonUtils.objectToJson(itemDesc));
			// 设置缓存的有效期
			jedisClient.expire(ITEM_INFO_PRE + ":" + itemid + ":DESC", ITEM_INFO_EXPIRE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return itemDesc;
	}
}
