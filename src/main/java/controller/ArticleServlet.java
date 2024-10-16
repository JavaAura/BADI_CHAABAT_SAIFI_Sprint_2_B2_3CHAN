package controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Article;
import model.Author;
import model.enums.ArticleStatus;
import repository.implementation.AuthorRepositoryImpl;
import repository.implementation.ArticleRepositoryImpl;
import repository.interfaces.ArticleRepository;
import repository.interfaces.AuthorRepository;
import service.ArticleService;
import service.AuthorService;

public class ArticleServlet extends HttpServlet {
	private static final Logger logger = LoggerFactory.getLogger(ArticleServlet.class);

	private final ArticleRepository articleRepository = new ArticleRepositoryImpl();
	private final AuthorRepository authorRepository = new AuthorRepositoryImpl();
	private final ArticleService articleService = new ArticleService(articleRepository);
	private final AuthorService authorService = new AuthorService(authorRepository);

	@Override

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("get request recieved");
		String action = request.getParameter("action");

		listArticles(request, response);

	}

	@Override

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = request.getParameter("action");
		HttpSession session = request.getSession();
		if ("add".equals(action)) {
			addArticle(request, response);

		} else if ("search".equals(action)) {
			searchArticles(request, response);

		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
		}
	}

	private void listArticles(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
		int pageSize = 4;

		List<Article> articles = articleService.getAllArticles(page, pageSize);

		List<Author> authors = authorService.getAllAuthors();

		Long totalArticles = articleService.getTotalArticleCount();
		int totalPages = (int) Math.ceil((double) totalArticles / pageSize);

		request.setAttribute("articles", articles);
		request.setAttribute("currentPage", page);
		request.setAttribute("totalPages", totalPages);
		request.setAttribute("authors", authors);

		request.getRequestDispatcher("/views/articles.jsp").forward(request, response);
	}

	private void searchArticles(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String title = request.getParameter("title");

		// Fetch all articles matching the search title
		List<Article> articles = articleService.searchArticleByTitle(title);
		List<Author> authors = authorService.getAllAuthors();

		// You can also set the title for display if needed
		request.setAttribute("articles", articles);
		request.setAttribute("searchTitle", title);
		request.setAttribute("authors", authors);

		// Directly forward to the JSP view
		request.getRequestDispatcher("/views/articles.jsp").forward(request, response);
	}

	private void addArticle(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String creationDateParam = request.getParameter("creation_date");
		String publicationDateParam = request.getParameter("publication_date");
		String articleStatutParam = request.getParameter("article_statut");
		String authorIdParam = request.getParameter("author_id");

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime creationDate = creationDateParam != null ? LocalDateTime.parse(creationDateParam, formatter)
				: LocalDateTime.now();
		LocalDateTime publicationDate = publicationDateParam != null
				? LocalDateTime.parse(publicationDateParam, formatter)
				: null;

		ArticleStatus statut = articleStatutParam != null ? ArticleStatus.valueOf(articleStatutParam.toUpperCase())
				: ArticleStatus.DRAFT;

		Long authorId = authorIdParam != null ? Long.parseLong(authorIdParam) : null;

		if (authorId == null) {
			request.setAttribute("error", "Author is required.");
			request.getRequestDispatcher("/views/articles.jsp").forward(request, response);
			return;
		}

		Author author = new Author();
		author.setId(authorId);

		Article article = new Article();
		article.setTitle(title);
		article.setContent(content);
		article.setCreationDate(creationDate);
		article.setPublicationDate(publicationDate);
		article.setArticleStatus(statut);
		article.setAuthor(author);

		articleService.addArticle(article);

		response.sendRedirect("articles?action=list&page=1");
	}

}
