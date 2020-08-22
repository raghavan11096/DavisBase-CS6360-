
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class CreateDCLCommand {
public static void createStatement (String createString) {
		
		System.out.println("CREATE METHOD");
		System.out.println("Parsing the string:\"" + createString + "\"");
		String[] tokens=createString.split(" ");
		
		if (tokens[1].compareTo("index")==0)
		{
		String col = tokens[4];
		String colName = col.substring(1,col.length()-1);
		Index.createIndex(tokens[3],colName, "String");
		}
		else
		{
		
		if (tokens[1].compareTo("table")>0) System.out.println("Wrong syntax");
		else{
				 
		String tableName = tokens[2];
		String[] temp = createString.split(tableName);
		String cols = temp[1].trim();
		String[] create_cols = cols.substring(1, cols.length()-1).split(",");

			Arrays.setAll(create_cols, i -> create_cols[i].trim());
		
		if(DavisBase.tableExists(tableName)) System.out.println("Table " + tableName + " already exists.");
		else
			{
				tableCreation(tableName,
						create_cols);
			}
			}
		}
	}
public static void tableCreation (String table, String[] col){
	try{	
		
		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");
		byteSetter(file);

		file = new RandomAccessFile("data/davisbase_tables.tbl", "rw");
		
		int numOfPages;
		numOfPages = Table.pages(file);
		int page=1;
		for(int p = 1; p <= numOfPages; p++){
			int rm = Page.getRightMost(file, p);
			if(rm == 0)
				page = p;
		}
		
		int[] keys = Page.getKeyArray(file, page);
		int l = keys[0];
		for(int i = 0; i < keys.length; i++)
			if(keys[i]>l)
				l = keys[i];
		file.close();
		
		String[] values = {Integer.toString(l+1), table};
		insertInto("davisbase_tables", values);

		file = new RandomAccessFile("data/davisbase_columns.tbl", "rw");
		
		numOfPages = Table.pages(file);
		page=1;
		for(int p = 1; p <= numOfPages; p++){
			int rm = Page.getRightMost(file, p);
			if(rm == 0)
				page = p;
		}
		
		keys = Page.getKeyArray(file, page);
		l = keys[0];
		for(int i = 0; i < keys.length; i++)
			if(keys[i]>l)
				l = keys[i];
		file.close();

		for(int i = 0; i < col.length; i++){
			l = l + 1;
			String[] token = col[i].split(" ");
			String col_name = token[0];
			String dt = token[1].toUpperCase();
			String pos = Integer.toString(i+1);
			String nullable;
			if(token.length > 2)
				nullable = "NO";
			else
				 nullable = "YES";
			String[] value = {Integer.toString(l), table, col_name, dt, pos, nullable};
			insertInto("davisbase_columns", value);
		}

	}catch(Exception e){
		System.out.println(e);
	}
}

	private static void byteSetter (RandomAccessFile file) throws IOException {
		file.setLength(Table.pageSize);
		file.seek(0);
		file.writeByte(0x0D);
		file.close();
	}

	public static void insertInto(String table, String[] values){
	try{
		RandomAccessFile file = new RandomAccessFile("data/"+table+".tbl", "rw");
		insertInto(file, table, values);
		file.close();

	}catch(Exception e){
		System.out.println(e);
	}
}
public static void insertInto(RandomAccessFile file, String table, String[] values){
	String[] dtype = Table.getDataType(table);
	String[] nullable = Table.getNullable(table);

	for(int i = 0; i < nullable.length; i++)
		if(values[i].equals("null") && nullable[i].equals("NO")){
			System.out.println("NULL-value constraint violation");
			System.out.println();
			return;
		}

	int key = new Integer(values[0]);
	int page = Table.searchKeyPage(file, key);
	if(page != 0)
		if(Page.hasKey(file, page, key)){
			System.out.println("Uniqueness constraint violation");
			return;
		}
	if(page == 0)
		page = 1;


	byte[] stc = new byte[dtype.length-1];
	short plSize = (short) Table.calPayloadSize(table, values, stc);
	int cellSize = plSize + 6;
	int offset = Page.checkLeafSpace(file, page, cellSize);


	if(offset != -1){
		Page.insertLeafCell(file, page, offset, plSize, key, stc, values);
	}else{
		Page.splitLeaf(file, page);
		insertInto(file, table, values);
	}
}

}
