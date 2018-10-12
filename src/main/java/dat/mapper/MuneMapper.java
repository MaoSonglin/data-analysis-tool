package dat.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import dat.pojo.Mune;
import dat.pojo.MuneExample;

@Mapper
public interface MuneMapper {
    int countByExample(MuneExample example);

    int deleteByExample(MuneExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(Mune record);

    int insertSelective(Mune record);

    List<Mune> selectByExample(MuneExample example);

    Mune selectByPrimaryKey(Integer id);
    
    List<Mune> listChildren(Integer id);

    int updateByExampleSelective(@Param("record") Mune record, @Param("example") MuneExample example);

    int updateByExample(@Param("record") Mune record, @Param("example") MuneExample example);

    int updateByPrimaryKeySelective(Mune record);

    int updateByPrimaryKey(Mune record);
}