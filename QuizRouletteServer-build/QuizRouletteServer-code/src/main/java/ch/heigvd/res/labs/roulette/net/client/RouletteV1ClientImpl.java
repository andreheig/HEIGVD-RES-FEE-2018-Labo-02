package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.StudentsStoreImpl;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV1Protocol;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.net.protocol.InfoCommandResponse;
import ch.heigvd.res.labs.roulette.net.protocol.RandomCommandResponse;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the client side of the protocol specification (version 1).
 * 
 * @author Olivier Liechti
 */
public class RouletteV1ClientImpl implements IRouletteV1Client {

  private static final Logger LOG = Logger.getLogger(RouletteV1ClientImpl.class.getName());
  private Socket client = null;
  protected OutputStream os = null;
  protected InputStream is = null;
  private boolean connected = false;
  protected ByteArrayOutputStream responseBuffer;
  protected byte[] buffer;
  protected int newBytes;

  final static int BUFFER_SIZE = 1024;

  @Override
  public void connect(String server, int port) throws IOException {
      client = new Socket(server, port);
      os = client.getOutputStream();
      is = client.getInputStream();
      connected = true;
      buffer = new byte[BUFFER_SIZE];
      responseBuffer = new ByteArrayOutputStream();
    responseBuffer.reset();
      newBytes = is.read(buffer);
      responseBuffer.write(buffer, 0, newBytes);
      String response = responseBuffer.toString().replace("\n", "").replace("\r", "");
  }

  @Override
  public void disconnect() throws IOException {
      os.write(RouletteV1Protocol.CMD_BYE.getBytes());
      client.close();
      connected = false;
  }

  @Override
  public boolean isConnected() {
      return connected;
  }

  @Override
  public void loadStudent(String fullname) throws IOException {
    responseBuffer.reset();
    if(fullname.length() == 0 || (fullname.replace(" ", "").length() == 0)) {
      return;
    }
      os.write((RouletteV1Protocol.CMD_LOAD + '\n').getBytes());
      os.flush();
      newBytes = is.read(buffer);
      responseBuffer.write(buffer, 0, newBytes);

      System.out.print(responseBuffer);
      String response = responseBuffer.toString().replace("\n", "").replace("\r", "");
    /**
     * On test que le serveur est prêt à reçevoir des noms
     */
    if(response.equalsIgnoreCase(RouletteV1Protocol.RESPONSE_LOAD_START)) {
        os.write((fullname + '\n').getBytes());
        os.flush();
        os.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + '\n').getBytes());
        os.flush();
        newBytes = is.read(buffer);
        responseBuffer.reset();
        responseBuffer.write(buffer, 0, newBytes);
        response = "";
        response = responseBuffer.toString().replace("\n", "").replace("\r", "");
      /*if(!response.equals(RouletteV1Protocol.RESPONSE_LOAD_DONE)){
          throw new IOException();
      }
      else{*/
        System.out.println(response);
        //}

    }
    else{
      throw new IOException();
    }
  }

  @Override
  public void loadStudents(List<Student> students) throws IOException {
    responseBuffer.reset();
    os.write((RouletteV1Protocol.CMD_LOAD + '\n').getBytes());
    os.flush();
    newBytes = is.read(buffer);
    responseBuffer.write(buffer, 0, newBytes);
    String response = responseBuffer.toString().replace("\n", "").replace("\r", "");
    if(response.equalsIgnoreCase(RouletteV1Protocol.RESPONSE_LOAD_START)) {
      System.out.print(responseBuffer);
      for (Student student : students) {
        os.write((student.getFullname() + '\n').getBytes());
      }
      os.write((RouletteV1Protocol.CMD_LOAD_ENDOFDATA_MARKER + '\n').getBytes());
      os.flush();
      newBytes = is.read(buffer);
      responseBuffer.write(buffer, 0, newBytes);
      System.out.print(responseBuffer);
    }
    else{
      throw new IOException();
    }
  }

  @Override
  public Student pickRandomStudent() throws EmptyStoreException, IOException {
    /**
     * On s'assure que le serveur contient des Etudiants, et donc on peut lui demander de nous retourner un
     * Etudiant au hasard
     */
    if(this.getNumberOfStudents() != 0){
      responseBuffer.reset();
        os.write((RouletteV1Protocol.CMD_RANDOM + '\n').getBytes());
        os.flush();
        newBytes = is.read(buffer);
        responseBuffer.write(buffer, 0, newBytes);
        Student student = JsonObjectMapper.parseJson(responseBuffer.toString(), Student.class);
        return student;
      }
      throw new EmptyStoreException();
  }

  @Override
  public int getNumberOfStudents() throws IOException {
    responseBuffer.reset();
    os.write((RouletteV1Protocol.CMD_INFO + '\n').getBytes());
    os.flush();
    newBytes = is.read(buffer);
    responseBuffer.write(buffer, 0, newBytes);
    System.out.print(responseBuffer);
    InfoCommandResponse info = JsonObjectMapper.parseJson(responseBuffer.toString(), InfoCommandResponse.class);
    return info.getNumberOfStudents();
  }

  @Override
  public String getProtocolVersion() throws IOException {
    return RouletteV1Protocol.VERSION;
  }



}
