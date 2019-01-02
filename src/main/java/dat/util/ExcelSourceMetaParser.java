package dat.util;

import dat.App;
import dat.domain.Source;
import dat.domain.UploadFile;
import dat.service.UploadFileService;

class ExcelSourceMetaParser extends MySQLSourceMetaData implements MetaDataParser{
	
	public ExcelSourceMetaParser(Source source) {
		Source s = new Source();
		BeanUtil.copyAttributes(source, s);
		UploadFile asso = source.getAssociation();
		String realPath = App.getContext().getBean(UploadFileService.class).getRealPath(asso.getId());
		
		// 不含后缀名的文件名
		int lastIndexOf = realPath.lastIndexOf('.');
		String substring = realPath.substring(0, lastIndexOf);
		
		String url = "jdbc:sqlite:"+substring+".db3";
		s.setUrl(url);
		s.setDriverClass("org.sqlite.JDBC");
		s.setDatabaseName("SQLite");
		setSource(s);
	}
}