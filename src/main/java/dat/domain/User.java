package dat.domain;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@Entity
@Table(name="t_user")
public class User implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8677548115671041462L;

	@Id
	private String id;
	
	private String username;
	
	private String password;
	
	private String salt;
	
	private Date createTime;
	
	private Integer role;
	
	private Integer state;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	

	public Integer getRole() {
		return role;
	}

	public void setRole(Integer role) {
		this.role = role;
	}

	public Date getCreateTime() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(Date.class);
		enhancer.setCallback(new MethodInterceptor() {
			@Override
			public Object intercept(Object arg0, Method arg1, Object[] arg2,
					MethodProxy arg3) throws Throwable {
				if(arg1.getName().equals("toString")){
					String format = new SimpleDateFormat("yyyy-MM-dd").format(createTime);
					return format;
				}
				return arg1.invoke(createTime, arg2);
			}
		});
		Date create = (Date) enhancer.create();
		return create;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	
	@Override
	public String toString() {
		return "User [id=" + id + ", username=" + username + ", password="
				+ password + ", salt=" + salt + ", createTime=" + createTime
				+ ", role=" + role + ", state=" + state + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
	
}
