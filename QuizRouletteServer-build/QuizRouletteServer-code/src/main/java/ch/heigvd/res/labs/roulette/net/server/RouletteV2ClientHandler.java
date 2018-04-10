package ch.heigvd.res.labs.roulette.net.server;

import ch.heigvd.res.labs.roulette.data.EmptyStoreException;
import ch.heigvd.res.labs.roulette.data.IStudentsStore;
import ch.heigvd.res.labs.roulette.data.JsonObjectMapper;
import ch.heigvd.res.labs.roulette.net.protocol.*;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the Roulette protocol (version 2).
 *
 * @author Olivier Liechti
 */
public class RouletteV2ClientHandler implements IClientHandler {

  final static Logger LOG = Logger.getLogger(RouletteV2ClientHandler.class.getName());

  private final IStudentsStore store;
  private int count;

  public RouletteV2ClientHandler(IStudentsStore store) {
    this.store = store;
    count = 0;
  }

  @Override
  public void handleClientConnection(InputStream is, OutputStream os) throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));

      writer.println("Hello. Online HELP is available. Will you find it?");
      writer.flush();

      String command;
      boolean done = false;
      while (!done && ((command = reader.readLine()) != null)) {
          LOG.log(Level.INFO, "COMMAND: {0}", command);
          switch (command.toUpperCase()) {
                  /*
                  No change
                   */
              case RouletteV2Protocol.CMD_RANDOM:
                  RandomCommandResponse rcResponse = new RandomCommandResponse();
                  try {
                      rcResponse.setFullname(store.pickRandomStudent().getFullname());
                  } catch (EmptyStoreException ex) {
                      rcResponse.setError("There is no student, you cannot pick a random one");
                  }
                  writer.println(JsonObjectMapper.toJson(rcResponse));
                  writer.flush();
                  count++;
                  break;
                  /*
                  No change
                   */
              case RouletteV2Protocol.CMD_HELP:
                  writer.println("Commands: " + Arrays.toString(RouletteV2Protocol.SUPPORTED_COMMANDS));
                  count++;
                  break;
                  /*
                  No change
                   */
              case RouletteV2Protocol.CMD_INFO:
                  InfoCommandResponse response = new InfoCommandResponse(RouletteV2Protocol.VERSION, store.getNumberOfStudents());
                  writer.println(JsonObjectMapper.toJson(response));
                  writer.flush();
                  count++;
                  break;
                  /*
                  Load OK
                   */
              case RouletteV2Protocol.CMD_LOAD:
                  writer.println(RouletteV2Protocol.RESPONSE_LOAD_START);
                  writer.flush();
                  int tmp = store.getNumberOfStudents();
                  store.importData(reader);
                  tmp = store.getNumberOfStudents() - tmp;
                  LoadCommandResponse lcResponse = new LoadCommandResponse("success", tmp);
                  //writer.println("{status\":\"success\",\"numberOfNewStudents\":" + tmp + "}");
                  writer.println(JsonObjectMapper.toJson(lcResponse));
                  writer.flush();
                  count++;
                  break;
                  /*
                  Bye OK
                   */
              case RouletteV2Protocol.CMD_BYE:
                  count++;
                  ByeCommandResponse bcResponse = new ByeCommandResponse("success", count);
                  writer.println(JsonObjectMapper.toJson(bcResponse));
                  //writer.println("{status\":\"success\",\"numberOfCommands\":" + count + "}");
                  done = true;
                  break;
                  /*
                  Clear OK
                   */
              case RouletteV2Protocol.CMD_CLEAR:
                  store.clear();
                  writer.println(RouletteV2Protocol.RESPONSE_CLEAR_DONE);
                  writer.flush();
                  count++;
                  break;
                  /*
                  List OK
                   */
              case RouletteV2Protocol.CMD_LIST:
                  writer.println(JsonObjectMapper.toJson(store.listStudents()));
                  writer.flush();
                  count++;
                  break;
              default:
                  writer.println("Huh? please use HELP if you don't know what commands are available.");
                  writer.flush();
                  break;
          }
          writer.flush();
      }

  }

}
