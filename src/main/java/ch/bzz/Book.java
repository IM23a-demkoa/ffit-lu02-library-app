package ch.bzz;

public class Book {
    int id;
    String isbn;
    String title;
    String author;
    int year;

     public Book(int id, String isbn, String title, String author, int year) {
         this.id = id;
         this.isbn = isbn;
         this.title = title;
         this.author = author;
         this.year = year;
     }

    @Override
    public String toString() {
        return title + " von " + author + " (" + year + ")";
    }
}
