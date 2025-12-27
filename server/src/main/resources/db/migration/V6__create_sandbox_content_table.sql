-- V6: Create sandbox_content table for storing regex/xpath examples
-- This table stores both built-in examples and user-uploaded/edited content

CREATE TABLE sandbox_content (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(20) NOT NULL CHECK (type IN ('REGEX_TEXT', 'HTML', 'XML')),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    content TEXT NOT NULL,
    source VARCHAR(20) NOT NULL CHECK (source IN ('BUILTIN', 'USER_UPLOADED', 'USER_EDITED')),
    owner_id BIGINT,
    category VARCHAR(100),
    difficulty_level INTEGER CHECK (difficulty_level BETWEEN 1 AND 5),
    original_content_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_sandbox_content_owner FOREIGN KEY (owner_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sandbox_content_original FOREIGN KEY (original_content_id) 
        REFERENCES sandbox_content(id) ON DELETE SET NULL,
    
    -- Business rule: built-in content must not have owner
    CONSTRAINT chk_builtin_no_owner CHECK (
        (source = 'BUILTIN' AND owner_id IS NULL) OR 
        (source != 'BUILTIN')
    ),
    
    -- Business rule: user content must have owner
    CONSTRAINT chk_user_content_has_owner CHECK (
        (source IN ('USER_UPLOADED', 'USER_EDITED') AND owner_id IS NOT NULL) OR 
        (source = 'BUILTIN')
    )
);

-- Create indexes for common queries
CREATE INDEX idx_sandbox_content_type_source ON sandbox_content(type, source);
CREATE INDEX idx_sandbox_content_owner_type ON sandbox_content(owner_id, type);
CREATE INDEX idx_sandbox_content_owner ON sandbox_content(owner_id);
CREATE INDEX idx_sandbox_content_category ON sandbox_content(category) WHERE category IS NOT NULL;
CREATE INDEX idx_sandbox_content_source ON sandbox_content(source);

-- Create index for finding forks
CREATE INDEX idx_sandbox_content_original ON sandbox_content(original_content_id) WHERE original_content_id IS NOT NULL;

-- Insert some initial built-in regex examples
INSERT INTO sandbox_content (type, name, description, content, source, category, difficulty_level) VALUES
    ('REGEX_TEXT', 
     'Email Addresses', 
     'A collection of email addresses for practicing regex patterns',
     E'john.doe@example.com\nadmin@company.co.uk\nuser+tag@subdomain.example.org\ninvalid@\n@invalid.com\nno-at-sign.com\nspaces in@email.com',
     'BUILTIN',
     'emails',
     2),
    
    ('REGEX_TEXT',
     'Phone Numbers',
     'Various phone number formats to practice with',
     E'+1 (555) 123-4567\n555-123-4567\n(555) 123 4567\n+44 20 7123 4567\n5551234567\n123-456\n(555)123-4567',
     'BUILTIN',
     'phone_numbers',
     2),
    
    ('REGEX_TEXT',
     'URLs and Web Addresses',
     'Different URL formats for regex practice',
     E'https://www.example.com\nhttp://example.com/path/to/page\nftp://files.example.org\nwww.example.com\nexample.com\nhttps://example.com:8080/path?query=value\nhttp://subdomain.example.co.uk',
     'BUILTIN',
     'urls',
     3),
    
    ('REGEX_TEXT',
     'Dates',
     'Various date formats',
     E'2024-01-15\n01/15/2024\n15-01-2024\n2024.01.15\nJanuary 15, 2024\n15 Jan 2024\n2024-13-45\n32/01/2024',
     'BUILTIN',
     'dates',
     3),
    
    ('REGEX_TEXT',
     'HTML Tags',
     'HTML tags for practicing tag matching',
     E'<div class="container">\n<p>Paragraph</p>\n<img src="image.jpg" alt="Image">\n<br>\n<input type="text" />\n< invalid tag >\n<unclosed\n<div>nested<span>tags</span></div>',
     'BUILTIN',
     'html',
     4);

-- Insert initial XPath examples (HTML)
INSERT INTO sandbox_content (type, name, description, content, source, category, difficulty_level) VALUES
    ('HTML',
     'Simple HTML Page',
     'A basic HTML page structure for XPath practice',
     E'<!DOCTYPE html>\n<html lang="en">\n<head>\n    <meta charset="UTF-8">\n    <title>Sample Page</title>\n</head>\n<body>\n    <header>\n        <h1>Welcome</h1>\n        <nav>\n            <a href="/">Home</a>\n            <a href="/about">About</a>\n        </nav>\n    </header>\n    <main>\n        <article>\n            <h2>First Article</h2>\n            <p class="intro">Introduction paragraph.</p>\n            <p>Content paragraph.</p>\n        </article>\n        <article>\n            <h2>Second Article</h2>\n            <p>Another paragraph.</p>\n        </article>\n    </main>\n    <footer>\n        <p>&copy; 2024</p>\n    </footer>\n</body>\n</html>',
     'BUILTIN',
     'html_basic',
     1);

-- Insert initial XPath examples (XML)
INSERT INTO sandbox_content (type, name, description, content, source, category, difficulty_level) VALUES
    ('XML',
     'Book Catalog',
     'A simple XML catalog of books for XPath practice',
     E'<?xml version="1.0" encoding="UTF-8"?>\n<catalog>\n    <book id="bk101">\n        <author>Gambardella, Matthew</author>\n        <title>XML Developer''s Guide</title>\n        <genre>Computer</genre>\n        <price>44.95</price>\n        <publish_date>2000-10-01</publish_date>\n    </book>\n    <book id="bk102">\n        <author>Ralls, Kim</author>\n        <title>Midnight Rain</title>\n        <genre>Fantasy</genre>\n        <price>5.95</price>\n        <publish_date>2000-12-16</publish_date>\n    </book>\n    <book id="bk103">\n        <author>Corets, Eva</author>\n        <title>Maeve Ascendant</title>\n        <genre>Fantasy</genre>\n        <price>5.95</price>\n        <publish_date>2000-11-17</publish_date>\n    </book>\n</catalog>',
     'BUILTIN',
     'xml_basic',
     1);

-- Add comment explaining the table structure
COMMENT ON TABLE sandbox_content IS 'Stores sandbox examples for regex and XPath practice, including both built-in examples and user-generated content';
COMMENT ON COLUMN sandbox_content.type IS 'Type of content: REGEX_TEXT for regex practice, HTML/XML for XPath practice';
COMMENT ON COLUMN sandbox_content.source IS 'Origin: BUILTIN (platform-provided), USER_UPLOADED (user file), USER_EDITED (forked builtin)';
COMMENT ON COLUMN sandbox_content.owner_id IS 'User who owns this content. NULL for built-in examples';
COMMENT ON COLUMN sandbox_content.original_content_id IS 'Reference to original content if this is a fork of a built-in example';