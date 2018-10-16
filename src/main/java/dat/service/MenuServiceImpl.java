package dat.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dat.mapper.MuneMapper;
import dat.pojo.Mune;
import dat.pojo.MuneExample;
import dat.pojo.MuneExample.Criteria;
import dat.pojo.Response;
import dat.util.Constant;

@Service("menuService")
public class MenuServiceImpl implements MenuService {
	
	@Resource(name="muneMapper")
	private MuneMapper muneMapper;
	
	public Response list(Integer menuid) {
		List<Mune> listChildren = muneMapper.listChildren(menuid);
		Response response = new Response(Constant.SUCCESS_CODE,"查询成功！",listChildren);
		return response;
	}
	
	@Transactional
	public Response addMenu(Mune menu) {
		MuneExample example = new MuneExample();
		Criteria criteria = example.createCriteria();
		criteria.andTextEqualTo(menu.getText());
		Integer parent = menu.getParent();
		if(parent != null)
			criteria.andParentEqualTo(parent);
		int countByExample = muneMapper.countByExample(example);
		if(countByExample > 0){
			return new Response(Constant.ERROR_CODE,String.format("名称为‘%s’的菜单项已经存在了！", menu.getText()));
		}
		muneMapper.insert(menu);
		List<Mune> m = muneMapper.selectByExample(example);
		return new Response(Constant.SUCCESS_CODE,"添加成功！",m.get(0));
	}

	@Transactional
	public Response update(Mune menu) {
		Mune mune = muneMapper.selectByPrimaryKey(menu.getId());
		if(mune == null){
			return new Response(Constant.ERROR_CODE,String.format("ID为‘%d’的菜单项不存在!", menu.getId()));
		}
		muneMapper.updateByPrimaryKey(menu);
		
		return new Response(Constant.SUCCESS_CODE,"修改成功！");
	}

	@Transactional
	public Response delete(Integer id) {
		Mune menu = muneMapper.selectByPrimaryKey(id);
		if(menu==null){
			return new Response(Constant.ERROR_CODE,String.format("ID‘%d’不存在！", id));
		}else{
			int deleteByPrimaryKey = muneMapper.deleteByPrimaryKey(id);
			return new Response(Constant.SUCCESS_CODE,String.format("删除‘%d’条记录！", deleteByPrimaryKey));
		}
	}

}
