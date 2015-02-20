package com.bookstore.web;
import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;

import com.bookstore.BookRepository;
import com.bookstore.Book;
import com.bookstore.JDBC;

@WebServlet("/book")
public class BookEditorServlet extends HttpServlet {

	@Inject @JDBC
	private BookRepository bookRepo;
		
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
	
	/** Prepare the book form before we display it. */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String id = request.getParameter("id");

		if (id != null && !id.isEmpty()) {
			Book book = bookRepo.lookupBookById(id);
			request.setAttribute("book", book);
			request.setAttribute("bookPubDate", dateFormat.format(book.getPubDate()));
		}

		/* Redirect to book-form. */
		getServletContext().getRequestDispatcher("/WEB-INF/pages/book-form.jsp").forward(
				request, response);

	}
	
	
	 /**
		 * Handles posting an HTML book form. If the id is null then it is an
		 * "add book", if the id is set then it is an "update book"
		 */
		protected void doPost(HttpServletRequest request,
				HttpServletResponse response) throws ServletException, IOException {

			String title = request.getParameter("title");
			String description = request.getParameter("description");
			String price = request.getParameter("price");
			String pubDate = request.getParameter("pubDate");
			

			String id = request.getParameter("id");
			if (id == null || id.isEmpty()) {
				bookRepo.addBook(title, description, price, pubDate);
			} else {
				bookRepo.updateBook(id, title, description, price, pubDate);
			}

			response.sendRedirect(request.getContextPath() + "/book/");
		}
}
