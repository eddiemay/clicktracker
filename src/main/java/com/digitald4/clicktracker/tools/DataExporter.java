package com.digitald4.clicktracker.tools;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.jdbc.DBConnectorThreadPoolImpl;
import com.digitald4.common.proto.DD4UIProtos.ListResponse;
import com.digitald4.common.storage.DAOSQLImpl;
import com.digitald4.common.tools.DataImporter;

public class DataExporter {
	public static void main(String[] args) throws Exception {
		DataImporter importer = new DataImporter(
				new DAOSQLImpl(new DBConnectorThreadPoolImpl("org.gjt.mm.mysql.Driver",
						"jdbc:mysql://localhost/budget?autoReconnect=true",
						"dd4_user", "getSchooled85")),
				"http://ct.digitald4.com/api");
		importer.login();
		ListResponse listResponse = importer.export(Click.class);

		// importer.runFor(GeneralData.class);
		// importer.runFor(Portfolio.class);
		// importer.runFor(Account.class);
		// importer.runFor(Template.class);
		// importer.runFor(TemplateBill.class);
		// importer.runFor(Bill.class);
	}
}
