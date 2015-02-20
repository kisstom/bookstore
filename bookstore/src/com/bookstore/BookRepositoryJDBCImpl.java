package com.bookstore;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.sql.DataSource;

import java.sql.Date;

@ApplicationScoped @JDBC
public class BookRepositoryJDBCImpl implements BookRepository {
	
	@Resource(name="jdbc/bookstore")
	private DataSource dataSource;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	@Override
	public Book lookupBookById(final String id) {
		return withDB(new JDBCRunner<Book>(){
			@Override
			public Book run(Connection connection) throws Exception {
				
				PreparedStatement prepareStatement = connection.prepareStatement("select * from book where id = ?");
				prepareStatement.setLong(1, Long.parseLong(id));
				ResultSet rs = prepareStatement.executeQuery();
						
				if (rs.next()) {
					Book book = new Book();
					book.setId("" + rs.getLong("id"));
					book.setPrice(rs.getBigDecimal("price"));
					book.setPubDate(rs.getDate("pubdate"));
					book.setTitle(rs.getString("title"));
					return book;
				} else {
					return null;
				}
				
			}});	
	}

	@Override
	public void addBook(final String title, final String description, final String price,
			final String pubDate) {
		withDB(new JDBCRunner<Book>(){
			@Override
			public Book run(Connection connection) throws Exception {
				
				PreparedStatement prepareStatement = connection.prepareStatement("insert into book (title, description, price, pubdate) values (?,?,?,?)");
				prepareStatement.setString(1, title);
				prepareStatement.setString(2, description);
				prepareStatement.setBigDecimal(3, new BigDecimal(price));
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateFormat.parse(pubDate));
				prepareStatement.setDate(4, new Date(calendar.getTimeInMillis()));
				
				
				int rowCount = prepareStatement.executeUpdate();
				if (rowCount!=1) {
					//throw new BookStoreException("Unable to insert book into bookstore");
				}
				return null;
			}});	

	}

	@Override
	public void updateBook(final String id, final String title, final String description,
			final String price, final String pubDate) {
		withDB(new JDBCRunner<Book>(){
			@Override
			public Book run(Connection connection) throws Exception {
				
				PreparedStatement prepareStatement = connection.prepareStatement("update book set  title=?, description=?, price=?, pubdate=? where id = ?");
				prepareStatement.setString(1, title);
				prepareStatement.setString(2, description);
				prepareStatement.setBigDecimal(3, new BigDecimal(price));
				
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(dateFormat.parse(pubDate));
				prepareStatement.setDate(4, new Date(calendar.getTimeInMillis()));
				
				prepareStatement.setLong(5, Long.parseLong(id));
				
				int rowCount = prepareStatement.executeUpdate();
				if (rowCount!=1) {
					//throw new BookStoreException("Unable to update book into bookstore");
				}
				return null;
			}});	

	}

	@Override
	public void removeBook(final String id) {
		withDB(new JDBCRunner<Book>(){
			@Override
			public Book run(Connection connection) throws Exception {
				
				PreparedStatement prepareStatement = connection.prepareStatement("delete from book where id = ?");
				prepareStatement.setLong(1, Long.parseLong(id));
				
				int rowCount = prepareStatement.executeUpdate();
				if (rowCount!=1) {
					//throw new BookStoreException("Unable to remove book from bookstore");
				}
				return null;
			}});	

	}
	

	@Override
	public List<Book> listBooks() {
		return doList(null);
	}

	/*@Override
	public List<Book> listBooksSortByPriceAsc() {
		return doList("price ASC");
	}

	@Override
	public List<Book> listBooksSortByTitleAsc() {
		return doList("title ASC");
	}

	@Override
	public List<Book> listBooksSortByTitleDesc() {
		return doList("title DESC");
	}

	@Override
	public List<Book> listBooksSortByPriceDesc() {
		return doList("price DESC");
	}

	@Override
	public List<Book> listBooksSortByPubDateDesc() {
		return doList("pubDate DESC");
	}

	@Override
	public List<Book> listBooksSortByPubDateAsc() {
		return doList("pubDate ASC");
	}*/
	
	
	static interface JDBCRunner <T> {
		T run(Connection connection) throws Exception;
	}

	private List<Book> doList(final String orderBy) {
		return withDB(new JDBCRunner<List<Book>>(){
			@Override
			public List<Book> run(Connection connection) throws Exception {
				List<Book> listing = new ArrayList<Book>();				
				Statement statement = connection.createStatement();
				final String query = "select * from book" + 
						(orderBy != null ? " ORDER BY " + orderBy + ";" : ";");
				ResultSet rs = statement.executeQuery(query);
				while (rs.next()) {
					Book book = new Book();
					book.setId("" + rs.getLong("id"));
					book.setPrice(rs.getBigDecimal("price"));
					book.setPubDate(rs.getDate("pubdate"));
					book.setTitle(rs.getString("title"));
					listing.add(book);
				} 
				return listing;
			}});		
	}

	private  <T> T withDB(JDBCRunner<T> runner) {
		Connection connection = null;
		try {
			connection = dataSource.getConnection();
			boolean auto = connection.getAutoCommit();
			connection.setAutoCommit(false);
			T result = runner.run(connection);
			connection.commit();
			connection.setAutoCommit(auto); //set it to what it was previously.
			return result;
		}catch (Exception ex) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				//should log this as a warn or info
			}
			//throw new BookStoreException(ex);
			return null;
		} finally {
			if (connection!=null) {
				try {
				connection.close();
				} catch (Exception ex) {
					//should log this as a warn or info
				}
			}
		}
	}


}

