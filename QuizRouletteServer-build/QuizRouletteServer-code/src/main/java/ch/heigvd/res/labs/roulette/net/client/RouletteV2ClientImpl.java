package ch.heigvd.res.labs.roulette.net.client;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.data.Student;
import ch.heigvd.res.labs.roulette.data.StudentsList;
import ch.heigvd.res.labs.roulette.net.protocol.RouletteV2Protocol;
import java.io.IOException;
import java.util.List;

/**
 * This class implements the client side of the protocol specification (version 2).
 *
 * @author Olivier Liechti
 */
public class RouletteV2ClientImpl extends RouletteV1ClientImpl implements IRouletteV2Client {

  @Override
  public void clearDataStore() throws IOException {
    responseBuffer.reset();
    os.write((RouletteV2Protocol.CMD_CLEAR + '\n').getBytes());
    os.flush();
    newBytes = is.read(buffer);
    responseBuffer.write(buffer, 0, newBytes);
    System.out.print(responseBuffer);
  }

  @Override
  public List<Student> listStudents() throws IOException {
    responseBuffer.reset();
    os.write((RouletteV2Protocol.CMD_LIST + '\n').getBytes());
    os.flush();
    newBytes = is.read(buffer);
    responseBuffer.write(buffer, 0, newBytes);
    System.out.print( "serveur response: " + responseBuffer);
    String response = responseBuffer.toString().replace("\n", "").replace("\r", "");
    StudentsList list = JsonObjectMapper.parseJson(response, StudentsList.class);
    System.out.println("Liste retourné : " + list.getStudents());
    //if(list.getStudents().size() != 0)
      return list.getStudents();
    //else
      //throw new EmptyStoreException();
  }

  @Override
  public String getProtocolVersion() throws IOException {
    return RouletteV2Protocol.VERSION;
  }
  
}
