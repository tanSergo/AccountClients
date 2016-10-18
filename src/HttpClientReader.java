/**
 * Created by Sergo on 14.10.2016.
 */

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.Random;

import static java.lang.System.exit;

public class HttpClientReader extends UntypedActor {

    final static Random random = new Random();

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    static class StartProcess {
        private Integer rCount;
        private Integer idFrom;
        private Integer idTill;

        public StartProcess(Integer rCount, Integer idFrom, Integer idTill){
            this.rCount = rCount;
            this.idFrom = idFrom;
            this.idTill = idTill;
        }

        @Override
        public String toString() {
            return "Process Reader {" +
                    "rCount =" + rCount + "}";
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartProcess) {
            log.info("Received {}", message);
            startRequest((StartProcess)message);
        }
        else unhandled(message);
        }

    private void startRequest(StartProcess message) {
        if (message.rCount>0){

            ActorRef anotherReader = getContext().actorOf(Props.create(HttpClientReader.class));
            anotherReader.tell(new HttpClientReader.StartProcess(message.rCount-1,message.idFrom,message.idTill), getSelf());
            try
            {
                // читаем файл с запросом в переменную header
                FileInputStream request = new FileInputStream("testrequest.txt");
                byte[] buffer = new byte[request.available()];
                int r = request.read(buffer);
                String header = new String(buffer, 0, r);
                request.close();

                // выделяем из строки запроса хост, порт и URL ресурса
                // для выделения используется специальнонаписанная ф-ия extract
                String host = Main.extract(header, "Host:", "\n");

                // если не найден параметр Host - ошибка
                if(host.equals(""))
                {
                    return;
                }
                int port = 8080;
                // открываем сокет
                Socket s = new Socket(host, port);

                // пишем туда HTTP request
                header =header.substring(0,header.indexOf("\r\n")) +
                        getRandomId(message.idFrom,message.idTill) + "/" + "\r\n"+
                        "Host: " + host + "\r\n\r\n";
                s.getOutputStream().write(header.getBytes());

                log.info("Work is done {}", message);
            }
            catch(Exception e)
            {e.printStackTrace();} // вывод исключений
        }
    }

    protected static Integer getRandomId(Integer idFrom, Integer idTill){
        return random.nextInt(idTill-idFrom+1) + idFrom;
    }

}
