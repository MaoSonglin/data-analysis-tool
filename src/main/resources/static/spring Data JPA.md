## 1. 什么是spring data jpa
&emsp;jpa既Java Persistence API。JPA是一个基于O/R映射的标准规范。所谓规范即只定义标准规则，比提供实现，软件提供商可以按照标准规范来实现，而使用者只需按照标准规范中定义的方式来使用，而不用和软件提供商打交道。JPA的主要实现由hibernate、EclipseLink和OpenJPA等，这也意味着我们只要使用JPA来开发，无论是哪一个开发方式都是一样的。</br>&emsp;Spring Data JPA是Spring Data的一个子项目，它通过提供基于JPA的Repository极大地减少JPA作为数据访问方案的代码量。
## 2. 定义数据访问层
&emsp;使用Spring Data JPA建立数据访问层十分简单，只需要定义一个继承JpaRepository的接口即可：</br>
```
public interface DsRepository extends JpaRepository<Source, String>,JpaSpecificationExecutor<Source> {

}
```
## 3. 配置使用spring data jpa
&emsp;在spring环境中，使用Spring Data JPA可以通过@EnableJpaRepositories注解来开启spring Data JPA的支持，@EnableJpaRepositories接收的value参数用来扫描数据访问层所在包下的数据访问的接口定义
```
@Configuration
@EnableJpaRepositories("com.wisely.repos")
public class JpaConfiguration{
	@Bean
	public EntityManagerFactory entityManagerFactory(){
		// ...
	}
	// 还需要配置DataSource、PlatformTransactionManager等相关必要的bean
}
```
## 4. 定义查询方法
&emsp;假设有以下数据表：</br>
字段名|字段类型|字段含义
-----:|:------:|-----
'id'|'Number'|主键
'AGE'|'Number'|年龄
'ADDRESS'|'varchar'|地址

## 5. 自定义repository的实现

