package database.helpers;

import java.sql.ResultSet;

public class ResultSetEnumerationHandler 
{
	public void onNext(ResultSet resultSet) throws Exception{}
	public void onError(Exception e){ e.printStackTrace(); }
}
