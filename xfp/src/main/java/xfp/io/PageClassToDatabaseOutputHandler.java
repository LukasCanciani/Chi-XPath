package xfp.io;

import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

//import it.uniroma3.hlog.HypertextualLogger;
import xfp.fixpoint.FixedPoint;
import xfp.fixpoint.PageClass;
import xfp.model.Webpage;
import xfp.util.Constants;
import xfp.util.XFPConfig;

public class PageClassToDatabaseOutputHandler<T> implements OutputHandler<T> {
//	static final private HypertextualLogger log = HypertextualLogger.getLogger();
	
	Properties properties; 

	private Connection con;

	private String type;
	
	static private PageClassToDatabaseOutputHandler<URI> nfpToFile = null;

    static private PageClassToDatabaseOutputHandler<String> dfpToFile = null;

    static private Map<String,Set<String>> pc_ids_written = new HashMap<>();

    private PageClassToDatabaseOutputHandler(String type)	{	
        this.type = type;
        pc_ids_written.put(type, new HashSet<>());
    }
   
    static public OutputHandler<URI> getNFPPageClassOutputHandler() {
        if (nfpToFile==null) 
            nfpToFile = new PageClassToDatabaseOutputHandler<>("nfp");
        return nfpToFile;
    }

    public static OutputHandler<String> getDFPPageClassOutputHandler() {
        if (dfpToFile==null)
            dfpToFile = new PageClassToDatabaseOutputHandler<>("dfp");
        return dfpToFile;
    }
	
	@Override
	public void storePageclass(PageClass<T> pc) throws Exception {
        if (pc_ids_written.get(type).contains(pc.getId()))	{
 //           log.trace("Page class "+pc.getId()+" already stored ("+type+").");
            return;
        }
        
        this.createConnection();
        
        final Set<Webpage> pages = pc.getPages();
        Set<FixedPoint<T>> allFP = pc.getVariant();
        //TODO allFP.addAll(pc.getConstant());
        
        this.writeFixedPoints(pc.getId(), pages, allFP);        
        
        this.closeConnection();
//        log.trace("Page class "+pc.getId()+" stored (pages "+pages.size()+") for type ("+type+"): "+allFP.size());
        pc_ids_written.get(type).add(pc.getId());	
	}
	
	public void createConnection() throws ClassNotFoundException, SQLException	{
		String url = XFPConfig.getString(Constants.DB_CONNECTION_STRING);
		Properties props = new Properties();
		props.setProperty("user",XFPConfig.getString(Constants.DB_USERNAME));
		props.setProperty("password",XFPConfig.getString(Constants.DB_PASSWORD));
		//props.setProperty("ssl","true");
		this.con = DriverManager.getConnection(url, props);
	}
	
	public void closeConnection() throws SQLException	{
		this.con.close();
	}
	
	 private void writeFixedPoints(String pc_id, Set<Webpage> pages, Set<FixedPoint<T>> fixedpoints) throws Exception {
        //fixedpoints to array
        final Map<String, List<String>> values = IOUtil.fixedpoints2MapSingle(pages, fixedpoints);
        List<String> headers = values.get(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER);
        headers = fillHeadersOfMulticolumnFixedpoints(headers);
        headers = FixedPointToSchemaElement.cleanHeader(headers);
        //headers = headers.stream().map(h -> FixedPointToSchemaElement.cleanSingleHeaderElement(h)).collect(Collectors.toList());
        String table;
		if (pages.isEmpty())
			throw new Exception("empty set of pages");
		else
			table = ((Webpage)pages.toArray()[0]).getWebsite().getName();
		
		String schema = XFPConfig.getString(Constants.DB_SCHEMA);
		
		table = table+"_"+pc_id;
        //TODO create table
        this.createTable(schema, table, headers);
        
        //TODO insert table
        Set<String> keys = values.keySet();
        keys.remove(PageClassFixedpointToFileOutputHandler.FIXEDPOINT_HEADER);
        for (String key : keys)	{
            // insert TODO
        	List<String> values_clean = cleanValues(values.get(key));
        	if (!values_clean.isEmpty()) {
        		String query = this.createInsertQuery(schema, table, headers, values_clean);
        		this.insertTuple(query);
        	} else {
        		values_clean.add(key);
        		String query = this.createInsertQuery(schema, table, headers, values_clean);
        		this.insertTuple(query);
        	}
        }
    }
	
	private List<String> cleanValues(List<String> list) {
		return list.stream().map(s -> s.replaceAll("\uFFFD", "\"").replaceAll("\'", "\"").replaceAll("\"", "\"")).collect(Collectors.toList());
	}

	private List<String> fillHeadersOfMulticolumnFixedpoints(List<String> headers) {
		String last = headers.get(0);
		for (int i=0; i<headers.size(); i++)	{
			if (headers.get(i).length()==0)	headers.set(i, last+i);
			else last = headers.get(i);
			//headers.set(i, headers.get(i).trim().toLowerCase().replaceAll("\\s",""));
		}
		return headers;
	}

		
	private boolean createTable(String schema, String table, List<String> headers)	{
		String sql = "";
		try {
			Statement s = con.createStatement();
			sql = this.createSQL(schema, table, headers);
			s.executeUpdate(sql);
			return true;
		} catch (SQLException e) {
			System.out.println(sql);
			e.printStackTrace();
		}
		return false;
	}
	
	private String createSQL(String schema, String table, List<String> headers)	{
		String query = "drop table if exists "+schema+"."+table+";"; 
		query += "create table "+schema+"."+table+" (";
		
		for (int i=0; i < headers.size()-1; i++)	{
			query += headers.get(i)+" text,";
		}
		query += headers.get(headers.size()-1)+" text);";
		
		return query;
	}
	
	private boolean insertTuple(String query)	{
		try {
			Statement s = con.createStatement();
			s.executeUpdate(query);
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private String createInsertQuery(String schema, String table, List<String> headers, List<String> values)	{
		String query = "insert into "+schema+"."+table+" (";
		
		for (int i=0; i < headers.size()-1; i++)	{
			query += headers.get(i)+",";
		}
		query += headers.get(headers.size()-1)+") values (";
		
		for (int i=0; i < values.size()-1; i++)	{
			if ((values.get(i)==null)||(values.get(i).isEmpty()))
				query += "null,";
			else 
				query += "\'"+values.get(i)+"\',";
		}
		query += "\'"+values.get(headers.size()-1)+"\');";
		return query;
	}
}
