## ADDED Requirements

### Requirement: Knowledge document storage
The system SHALL store medical knowledge documents in a knowledge_documents table with fields: id (auto-increment), title (varchar), content (text), category (varchar), tags (text array), embedding (vector(1536) using pgvector), is_active (boolean), created_at, updated_at.

#### Scenario: Store knowledge document with embedding
- **WHEN** a knowledge document is added to the system
- **THEN** the system SHALL generate an embedding vector from the document content using an embedding API and store it in the embedding column

### Requirement: Semantic search for RAG
The system SHALL support semantic similarity search against knowledge documents using pgvector cosine distance.

#### Scenario: Search relevant documents
- **WHEN** a user message is embedded and searched against knowledge_documents
- **THEN** the system SHALL return the top K (default 3) documents with cosine similarity > 0.75, ordered by similarity descending

#### Scenario: Filter by active status
- **WHEN** performing a semantic search
- **THEN** the system SHALL only search documents where is_active=true

#### Scenario: Filter by category
- **WHEN** performing a semantic search with a category filter
- **THEN** the system SHALL restrict results to documents matching the specified category

### Requirement: Embedding generation
The system SHALL generate embedding vectors for user queries and knowledge documents using an external embedding API.

#### Scenario: Generate query embedding
- **WHEN** the RAG retriever needs to search for relevant documents
- **THEN** the system SHALL embed the user message text using the configured embedding model (e.g., text-embedding-3-small) and use the resulting vector for cosine similarity search

### Requirement: pgvector extension setup
The system SHALL use the pgvector PostgreSQL extension for vector storage and similarity search.

#### Scenario: Database migration enables pgvector
- **WHEN** the application starts for the first time
- **THEN** the database migration SHALL execute `CREATE EXTENSION IF NOT EXISTS vector` to enable pgvector support
