# Information Retrieval & Text Mining

## Project Requirements
1. **Indexer**: Starts from a set of documents and creates an “inverted index”.
2. **Searcher**: Starts from an “inverted index” (previously created by the Indexer) and can respond to any number of queries.

---

## How to Run

The project was made in IntelliJ IDEA. To run this project, use the following commands:

```bash
# Step 1: Build the project
mvn package

# Step 2: Index documents
java -jar target/docsearch-1.0-SNAPSHOT.jar -index -directory <path to docs>

# Step 3: Search for a keywords
java -jar target/docsearch-1.0-SNAPSHOT.jar -search -query <keyword>
```
---

## Libraries Used

1. **Apache Lucene**:
    - Used for creating, storing, and querying the inverted index with tools like `IndexWriter`, `RomanianAnalyzer`, and `QueryParser`.

2. **Apache Tika**:
    - Used for extracting text content from various document formats (`TXTParser`, `PDFParser`, `OOXMLParser`) and handling metadata.

---

## Main Class

The Main class serves as the entry point for the application, allowing users to index documents or search an inverted index through command-line arguments.

---

## Indexer Class

The `Indexer` class is a key component of the application responsible for processing documents and creating an **inverted index**. This index serves as the foundation for efficient search queries. It supports a variety of file formats, including `.txt`, `.pdf`, `.doc`, and `.docx`, and uses Romanian-specific text analysis with stopwords filtering and diacritics elimination.



### File Processing Workflow

**Stopwords Handling**:
    - Reads the stopwords file to filter out irrelevant terms.

 **Content Extraction**:
    - Uses Apache Tika to extract text content based on the file type.

**Document Creation**:
    - Normalizes content by removing diacritics.
    - Adds metadata (filename, file path) to the index. 

 **Index Writing**:
    - Stores the processed documents in an inverted index for fast retrieval.

### Steps
**Document Indexing**:
    - Reads documents from a specified directory.
    - Creates an inverted index containing document metadata like filename and path and normalized content.

**File Format Support**:
    - Supported formats: `.txt`, `.pdf`, `.doc`, `.docx`.
    - Unsupported formats are skipped with a message.

**Romanian-Specific Text Analysis**:
    - Uses `RomanianAnalyzer` to tokenize and analyze text.
    - Removes stopwords like "și", "sau" defined in .txt file taken from: [Romanian Stopwords List](https://countwordsfree.com/stopwords/romanian) and placed in org.example as required.

**Error Handling**:
    - Validates the existence of input files and stopword files.
    - Logging issues without interrupting the process.

### Methods
#### `Indexer`
- Initializes the `Indexer` by preparing the inverted index in the specified output directory.

#### `indexFiles`
- Indexes all valid files from the specified directory.
- Returns `true` if successful, otherwise `false`.

#### `makeDocument`
- Creates a `Document` object containing the filename, file path, and normalized content.

#### `getContent`
- Extracts the content of a file based on its format using Apache Tika parsers (`TXTParser`, `PDFParser`, `OOXMLParser`).

#### `eliminateDiacritics`
- Removes diacritical marks from Romanian text for consistent indexing and searching.

---

# Searcher Class

The `Searcher` class is responsible for querying an inverted index and retrieving relevant results based on a query. It uses Lucene's search capabilities and is made for Romanian text analysis.

## Workflow

**Index Loading**:
    - Loads the inverted index from the specified directory.
    - Uses `DirectoryReader` to verify the existence of the index.

**Query Processing**:
    - Accepts the provided query string.
    - Parses and normalizes the query using the `RomanianAnalyzer`.

**Search Execution**:
    - Performs a search on the index and retrieves the top 5 results.

**Results Display**:
    - Fetches document metadata for the top results and prints them.


## Steps

**Load Index**:
    - Initializes and loads an existing inverted index from a specified directory.
    - Verifies if the index is available before performing any operations.

**Search Functionality**:
    - Processes the queries and returns the most relevant results.
    - Supports Romanian-specific text analysis using the `RomanianAnalyzer`.

**Results Handling**:
    - Retrieves and prints document metadata for matching results.


### Methods

#### `Searcher`
- Initializes the searcher with the specified index directory.

#### `searching`
- Processes the search query and retrieves the top results.
- Validates whether the index is loaded before performing the search.

#### `makeQuery`
- Converts the raw query string into a Lucene `Query` object using the `RomanianAnalyzer`.

#### `executingSearch`
- Executes the search operation and returns the top 5 results.

#### `printResults`
- Prints the filenames of the top matching documents.

#### `getDoc`
- Retrieves the `Document` object for a given document ID from the index.

