package repository.interfaces;

import model.Author;

import java.util.List;

public interface AuthorRepository {
    public void addAuthor(Author author);
    public void updateAuthor(Author author);
    public void deleteAuthor(Long authorId);
    public List<Author> getAllAuthors();

}
