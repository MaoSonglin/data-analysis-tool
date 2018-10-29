var databaseProductNames = {
	server : ['MySQL','Oracle','SQL Server','DB2','hive','Hbase','MongoDB'],
	file : ['Access','SQLite','Excel']
}
var driverInfos = {
	"MySQL" : {
		"driverClass" : "com.mysql.jdbc.Driver",
		"url" : "jdbc:mysql://localhost:3306/test"
	},
	"Oracle" : {
		"driverClass" : "oracle.jdbc.driver.OracleDriver",
		"url" : "jdbc:oracle:thin@localhost:1521:XE"
	},
	'SQL Server':{
		"driverClass" : "com.microsoft.jdbc.sqlserver.SQLServerDriver",
		"url" : "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=tempdb"
	},
	"hive" : {
		"driverClass" : "org.apache.hadoop.hive.jdbc.HiveDriver",
		"url" : "jdbc:hive2://localhost:10002/default"
	},
	"DB2" : {
		"driverClass" : "com.ibm.db2.jcc.DB2Driver",
		"url" : "jdbc:db2://localhost:50000/toolsdb"
	},
	"SQLite" : {
		"driverClass" : "org.sqlite.JDBC",
		"url" : "jdbc:sqlite:test.db"
	},
	"infoxmix" : {
		"driverClass" : "com.informix.jdbc.IfxDriver",
		"url" : "jdbc:informix-sqli://127.0.0.1:1533/testDB:INFORMIXSERVER=myserver;"
	},
	"postgre" : {
		"driverClass" : "org.postgresql.Driver",
		"url" : "jdbc:postgresql://127.0.0.1:5432/postgres"
	},
	"Excel" : {
		"driverClass" : "",
		"url" : ""
	}
}
var keySet = {
	"id" : "编号",
	"name" : "数据源名称",
	"chinese" : "中文名称",
	"jdbcDirver" : "jdbc驱动包",
	"driverClass" : "驱动类",
	"url":"数据源URL",
	"username":"用户名",
	"password":"连接密码",
	"charset" : "字符集",
	"sortRule" : "排序规则",
	"databaseName" : "DBMS名称",
	"addTime" : "添加时间",
	"state" : "状态",
	"association" : "数据库文件"
}
var dsList = [ {
      "id" : "201810180920303570",
      "name" : "不好过好几个",
      "chinese" : "",
      "jdbcDirver" : "",
      "driverClass" : "com.mysql.jdbc.Driver",
      "url" : "jdbc:mysql://localhost:3306/test",
      "username" : "的发送到发",
      "password" : "dasfd ",
      "charset" : "",
      "sortRule" : "",
      "databaseName" : "MySQL",
      "addTime" : "",
      "state" : 1,
      "association" : null
    }, {
      "id" : "201810190852496209",
      "name" : "都是数据源",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : null,
      "url" : "23424",
      "username" : null,
      "password" : null,
      "charset" : null,
      "sortRule" : null,
      "databaseName" : null,
      "addTime" : null,
      "state" : 1,
      "association" : null
    }, {
      "id" : "201810210206418980",
      "name" : "SQL server数据源",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : "com.microsoft.jdbc.sqlserver.SQLServerDriver",
      "url" : "jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=tempdb",
      "username" : "sa",
      "password" : "admin",
      "charset" : null,
      "sortRule" : null,
      "databaseName" : "SQL Server",
      "addTime" : "2018-10-21 02:06:41",
      "state" : 1,
      "association" : null
    }, {
      "id" : "201810210209426169",
      "name" : "年终报表",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : null,
      "url" : null,
      "username" : null,
      "password" : null,
      "charset" : null,
      "sortRule" : null,
      "databaseName" : "Access",
      "addTime" : "2018-10-21 02:09:42",
      "state" : 1,
      "association" : {
        "id" : "201810210303209070",
        "fileName" : "setup.h.in",
        "virtualPath" : "2018-10-21\\setup.h.in",
        "size" : 26299,
        "state" : 1,
        "addTime" : "2018-10-21 15:03:20"
      }
    }, {
      "id" : "201810210211492810",
      "name" : "年终报表2",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : null,
      "url" : null,
      "username" : null,
      "password" : null,
      "charset" : null,
      "sortRule" : null,
      "databaseName" : "Excel",
      "addTime" : "2018-10-21 02:11:49",
      "state" : 1,
      "association" : {
        "id" : "201810210411319454",
        "fileName" : "form.swf",
        "virtualPath" : "2018-10-21\\form.swf",
        "size" : 185432,
        "state" : 1,
        "addTime" : "2018-10-21 16:11:31"
      }
    }, {
      "id" : "201810210212417630",
      "name" : "年终报表3",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : null,
      "url" : null,
      "username" : null,
      "password" : null,
      "charset" : null,
      "sortRule" : null,
      "databaseName" : "Excel",
      "addTime" : "2018-10-21 02:12:41",
      "state" : 1,
      "association" : {
        "id" : "201810210256148789",
        "fileName" : "configure.in",
        "virtualPath" : "2018-10-21\\configure.in",
        "size" : 314259,
        "state" : 1,
        "addTime" : "2018-10-21 14:56:14"
      }
    }, {
      "id" : "201810210215221148",
      "name" : "即可将立即",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : "org.apache.hadoop.hive.jdbc.HiveDriver",
      "url" : "jdbc:hive2://localhost:10002/default",
      "username" : null,
      "password" : null,
      "charset" : null,
      "sortRule" : null,
      "databaseName" : "hive",
      "addTime" : "2018-10-21 02:15:22",
      "state" : 1,
      "association" : null
    }, {
      "id" : "201810210215544049",
      "name" : "交流空间来看",
      "chinese" : null,
      "jdbcDirver" : null,
      "driverClass" : null,
      "url" : null,
      "username" : null,
      "password" : null,
      "charset" : null,
      "sortRule" : null,
      "databaseName" : "Excel",
      "addTime" : "2018-10-21 02:15:54",
      "state" : 1,
      "association" : {
        "id" : "201810210346410594",
        "fileName" : "fr.mo",
        "virtualPath" : "2018-10-21\\fr.mo",

        "size" : 138149,

        "state" : 1,

        "addTime" : "2018-10-21 15:46:41"

      }

    }, {

      "id" : "201810210356162857",

      "name" : "发光时代分公司电饭锅",

      "chinese" : null,

      "jdbcDirver" : null,

      "driverClass" : null,

      "url" : null,

      "username" : null,

      "password" : null,

      "charset" : null,

      "sortRule" : null,

      "databaseName" : "Access",

      "addTime" : "2018-10-21 03:56:16",

      "state" : 1,

      "association" : {

        "id" : "201810210356089347",

        "fileName" : "wxstd.pot",

        "virtualPath" : "2018-10-21\\wxstd.pot",

        "size" : 188993,

        "state" : 1,

        "addTime" : "2018-10-21 15:56:08"

      }

    }, {

      "id" : "201810210412483207",

      "name" : "士大夫的",

      "chinese" : null,

      "jdbcDirver" : null,

      "driverClass" : null,

      "url" : null,

      "username" : null,

      "password" : null,

      "charset" : null,

      "sortRule" : null,

      "databaseName" : "Access",

      "addTime" : "2018-10-21 04:12:48",

      "state" : 1,

      "association" : {

        "id" : "201810210412313675",

        "fileName" : "makefile.unx",

        "virtualPath" : "2018-10-21\\makefile.unx",

        "size" : 2702,

        "state" : 1,

        "addTime" : "2018-10-21 16:12:31"

      }

    }, {

      "id" : "201810220248500472",

      "name" : "测试读取字段",

      "chinese" : null,

      "jdbcDirver" : null,

      "driverClass" : "com.mysql.jdbc.Driver",

      "url" : "jdbc:mysql://39.106.26.170:3306/iqaa",

      "username" : "root",

      "password" : "123456",

      "charset" : null,

      "sortRule" : null,

      "databaseName" : "MySQL",

      "addTime" : "2018-10-22 02:48:50",

      "state" : 1,

      "association" : null

    } ]
