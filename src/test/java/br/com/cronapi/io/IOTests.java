package br.com.cronapi.io;

import cronapi.Var;
import cronapi.io.Operations;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class IOTests {

  private Var file;
  private Var folder;
  private Var fileObject;
  private Var folderObject;

  @Before
  public void setUp() throws Exception {
    file = Var.valueOf("/folder/sub/filename.ext");
    folder = Var.valueOf("/folder/sub");
    fileObject = Var.valueOf(new File("/folder/sub/filename.ext"));
    folderObject = Var.valueOf(new File("/folder/sub"));
  }

  @Test
  public void testGetFileName() {
    Assert.assertEquals(Operations.getFileName(file).getObjectAsString(), "filename.ext");
    Assert.assertEquals(Operations.getFileName(fileObject).getObjectAsString(), "filename.ext");
  }

  @Test
  public void testgetParent() {
    Assert.assertEquals(Operations.getParent(file).getObjectAsString(), "/folder/sub");
    Assert.assertEquals(Operations.getParent(fileObject).getObjectAsString(), "/folder/sub");
  }

  @Test
  public void testToFile() {
    Assert.assertEquals(Operations.toFile(folder, Var.valueOf("filename.ext")).getObjectAsString(), "/folder/sub/filename.ext");
    Assert.assertEquals(Operations.toFile(folderObject, Var.valueOf("filename.ext")).getObjectAsString(), "/folder/sub/filename.ext");
  }

  @Test
  public void testFileExtension() {
    Assert.assertEquals(Operations.getFileExtension(file).getObjectAsString(), "ext");
    Assert.assertEquals(Operations.getFileExtension(folder).getObjectAsString(), "");
    Assert.assertEquals(Operations.getFileExtension(fileObject).getObjectAsString(), "ext");
    Assert.assertEquals(Operations.getFileExtension(folderObject).getObjectAsString(), "");
  }


}