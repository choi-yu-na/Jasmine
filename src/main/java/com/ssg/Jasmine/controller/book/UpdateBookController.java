package com.ssg.Jasmine.controller.book;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import com.ssg.Jasmine.controller.user.UserSession;
import com.ssg.Jasmine.domain.Book;
import com.ssg.Jasmine.domain.Category;
import com.ssg.Jasmine.service.BookService;
import com.ssg.Jasmine.service.CategoryService;

@Controller
@RequestMapping("/book/update/{bookId}")
public class UpdateBookController implements ApplicationContextAware{
	@Value("/images/")
	private String uploadDirLocal;
//	파일 업로드 위한 변수
	private WebApplicationContext context;	
	private String uploadDir;
	
	@Autowired
	BookService bookService;
	@Autowired
	CategoryService categoryService;
	
	public void setApplicationContext(ApplicationContext appContext)
		throws BeansException {
		this.context = (WebApplicationContext) appContext;
		this.uploadDir = context.getServletContext().getRealPath(this.uploadDirLocal);
		System.out.println(this.uploadDir);
	}
	
	@ModelAttribute("genres")
	public List<Category> genreList(HttpServletRequest request) throws Exception {
		List<Category> genres = categoryService.getAllGenres();
		return genres;
	}
	
	@ModelAttribute("bookForm")
	public BookForm formBackingObect(HttpServletRequest request) throws Exception {
		return new BookForm();
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String updateForm (@PathVariable("bookId") int bookId, ModelMap model,HttpSession session) {
		UserSession userSession  = (UserSession)session.getAttribute("userSession");
		String userId = userSession.getUser().getUserId();

		Book book = bookService.getBookByBookId(bookId);
		
		if(userId.equals(book.getUserId())) {
			BookForm bookForm=new BookForm();

			bookForm.setIsbn(book.getIsbn());
			bookForm.setPrice(book.getPrice());
			bookForm.setCategoryId(book.getCategoryId());
			bookForm.setTitle(book.getTitle());
			bookForm.setAuthor(book.getAuthor());
			bookForm.setPublisher(book.getPublisher());
			bookForm.setImg(book.getImg());
			System.out.println("이미지"+book.getImg());
			
			model.addAttribute("bookForm",bookForm);
			System.out.println("get 호출됨");
			
			return "book/update";
		}
		else {
			model.addAttribute("bookId", bookId);
			return "book/update_error";
		}

			
		
	}
	
	@RequestMapping(method=RequestMethod.POST)
	public String submit(HttpServletRequest request, HttpSession session,
			@ModelAttribute("bookForm") BookForm bookForm, BindingResult result,
			Model model, SessionStatus sessionStatus) throws Exception {
		
		MultipartFile report = bookForm.getReport();
		String filename = uploadFile(report);
		model.addAttribute("fileUrl", this.uploadDirLocal + filename);

		UserSession user = (UserSession)request.getSession().getAttribute("userSession");
		String userId = user.getUser().getUserId();
		session.setAttribute("bookForm", bookForm);
		
		//String bookId =bookForm.getTitle();
		model.addAttribute("userId",userId);
		model.addAttribute("bookForm", bookForm);
		
		//String genre = categoryService.getGenreByCategoryId(bookForm.getCategoryId());
		
		Book book = new Book();
		book.setIsbn(bookForm.getIsbn());
		book.setPrice(bookForm.getPrice());
		book.setCategoryId(bookForm.getCategoryId());
		book.setTitle(bookForm.getTitle());
		book.setAuthor(bookForm.getAuthor());
		book.setPublisher(bookForm.getPublisher());
		book.setUserId(userId);
		book.setImg(this.uploadDirLocal + filename);
		
		System.out.println("img: "+book.getImg());
		
		
		
		System.out.println(userId);
		
		bookService.createBook(book);
		
		
		sessionStatus.setComplete();
		
		
		return "redirect:/book/detail/"+book.getBookId(); //리다이렉트 하는게 나을듯?
	}
	
	private String uploadFile(MultipartFile report) {
		String filename = UUID.randomUUID().toString() 
						+ "_" + report.getOriginalFilename();
		File file = new File(this.uploadDir + filename);
		try {
			report.transferTo(file);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
		}
		return filename;
	}
}