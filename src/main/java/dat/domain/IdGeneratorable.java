package dat.domain;

import java.io.Serializable;

/**
 * @author MaoSonglin
 * 生成实体类ID的接口
 */
public interface IdGeneratorable extends Serializable {
	void generateId();
}
