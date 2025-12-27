package hu.kornel.server.domain.exception;


public class ContentTooLargeException extends RuntimeException {
    
    private final long actualSize;
    private final long maxSize;
    
    public ContentTooLargeException(long actualSize, long maxSize) {
        super(String.format("Content size %d bytes exceeds maximum allowed size of %d bytes", 
                actualSize, maxSize));
        this.actualSize = actualSize;
        this.maxSize = maxSize;
    }
    
    public long getActualSize() {
        return actualSize;
    }
    
    public long getMaxSize() {
        return maxSize;
    }
}