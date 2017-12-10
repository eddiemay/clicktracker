package com.digitald4.clicktracker.tools;

import com.digitald4.clicktracker.proto.ClickTrackerProtos.Click;
import com.digitald4.common.jdbc.DBConnectorThreadPoolImpl;
import com.digitald4.common.storage.DAOSQLImpl;
import com.digitald4.common.tools.DataImporter;
import java.io.FileWriter;
import java.io.IOException;
import org.joda.time.DateTime;

public class DataExporter {
	private static final String CSV = "%s, %s, %s\n";
	public static void main(String[] args) throws Exception {
		DataImporter importer = new DataImporter(
				new DAOSQLImpl(new DBConnectorThreadPoolImpl("org.gjt.mm.mysql.Driver",
						"jdbc:mysql://localhost/budget?autoReconnect=true",
						"dd4_user", "getSchooled85")),
				"http://ct.digitald4.com/api");
		importer.login();
		FileWriter writer = new FileWriter("data.csv");
		writer.write(String.format(CSV, "Recorded", "Language", "URL"));
		importer.export(Click.class)
				.getResultList()
				.forEach(any -> {
					try {
						Click click = any.unpack(Click.class);
						DateTime recorded = new DateTime(click.getRecorded());
						writer.write(String.format(CSV, recorded.toString("MM/dd/yyyy HH:mm:ss"), click.getAcceptLanguage(), click.getUrl()));
					} catch (IOException ioe) {
						ioe.printStackTrace();
					}
				});
	}
}
