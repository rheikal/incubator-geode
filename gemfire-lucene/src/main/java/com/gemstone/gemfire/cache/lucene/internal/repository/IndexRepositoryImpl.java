package com.gemstone.gemfire.cache.lucene.internal.repository;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import com.gemstone.gemfire.cache.lucene.internal.repository.serializer.LuceneSerializer;
import com.gemstone.gemfire.cache.lucene.internal.repository.serializer.SerializerUtil;

/**
 * A repository that writes to a single lucene index writer
 */
public class IndexRepositoryImpl implements IndexRepository {
  
  private static final boolean APPLY_ALL_DELETES = System
      .getProperty("gemfire.IndexRepository.APPLY_ALL_DELETES", "true")
      .equalsIgnoreCase("true");
  
  private final IndexWriter writer;
  private final LuceneSerializer serializer;
  private volatile DirectoryReader reader;
  private volatile IndexSearcher searcher;
  
  public IndexRepositoryImpl(IndexWriter writer, LuceneSerializer serializer) throws IOException {
    this.writer = writer;
    reader = DirectoryReader.open(writer, APPLY_ALL_DELETES);
    this.serializer = serializer;
  }

  @Override
  public void create(Object key, Object value) throws IOException {
      Document doc = new Document();
      SerializerUtil.addKey(key, doc);
      serializer.toDocument(value, doc);
      writer.addDocument(doc);
  }

  @Override
  public void update(Object key, Object value) throws IOException {
    Document doc = new Document();
    SerializerUtil.addKey(key, doc);
    serializer.toDocument(value, doc);
    writer.updateDocument(SerializerUtil.getKeyTerm(doc), doc);
  }

  @Override
  public void delete(Object key) throws IOException {
    Term keyTerm = SerializerUtil.toKeyTerm(key);
    writer.deleteDocuments(keyTerm);
  }

  @Override
  public void query(Query query, int limit, IndexResultCollector collector) throws IOException {
    IndexSearcher searcherSnapshot = searcher;
    TopDocs docs = searcherSnapshot.search(query, limit);
    for(ScoreDoc scoreDoc : docs.scoreDocs) {
      Document doc = searcher.doc(scoreDoc.doc);
      Object key = SerializerUtil.getKey(doc);
      collector.collect(key, scoreDoc.score);
    }
  }

  @Override
  public synchronized void commit() throws IOException {
    writer.commit();
    DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
    if(newReader != null) {
      reader = newReader;
      searcher = new IndexSearcher(reader);
    }
  }
}