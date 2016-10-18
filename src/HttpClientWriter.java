/**
 * Created by Sergo on 03.10.2016.
 */

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import scala.concurrent.Future;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;

import static java.lang.System.exit;

public class HttpClientWriter extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    static class StartProcess {
        private Integer wCount;
        private Integer idFrom;
        private Integer idTill;

        public StartProcess(Integer wCount, Integer idFrom, Integer idTill){
            this.wCount = wCount;
            this.idFrom = idFrom;
            this.idTill = idTill;
        }

        @Override
        public String toString() {
            return "Process Writer {" +
                    "wCount =" + wCount + "}";
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

    private void startRequest(HttpClientWriter.StartProcess message) {
        if (message.wCount>0){

            ActorRef anotherWriter = getContext().actorOf(Props.create(HttpClientWriter.class));
            anotherWriter.tell(new HttpClientWriter.StartProcess(message.wCount-1,message.idFrom,message.idTill), getSelf());
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
                        HttpClientReader.getRandomId(message.idFrom,message.idTill) + "/" + getRandomValue() + "\r\n"+
                        "Host: " + host + "\r\n\r\n";
                s.getOutputStream().write(header.getBytes());

                log.info("Work is done {}", message);
            }
            catch(Exception e)
            {e.printStackTrace();} // вывод исключений
        }
    }


    protected static Integer getRandomValue(){
        return HttpClientReader.random.nextInt(50000 - (-50000) + 1) + (-50000);
    }
}

