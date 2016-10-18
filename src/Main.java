/**
 * Created by Sergo on 03.10.2016.
 */
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.*;

public class Main {

    public static void main(String[] args) {
        try {
            FileInputStream config = new FileInputStream("config.txt");
            byte[] buffer = new byte[config.available()];
            // считаем файл в буфер
            int r = config.read(buffer, 0, config.available());
            String header = new String(buffer, 0, r);

            Integer rCount = Integer.parseInt(extract(header, "rCount:", "\n"));
            Integer wCount = Integer.parseInt(extract(header, "wCount:", "\n"));
            Integer idFrom = Integer.parseInt(extract(header, "FROM", " TILL"));
            Integer idTill = Integer.parseInt(extract(header, "TILL", "\n"));


            ActorSystem system = ActorSystem.create("HttpClients");
            ActorRef reader = system.actorOf(Props.create(ClientsCreator.class), "Creator");
            Future<Object> future1 = Patterns.ask(reader,
                    new ClientsCreator.StartCreating(rCount,wCount,idFrom,idTill), 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * "Вырезает" из строки str часть, находящуюся между строками start и end.
     * Если строки end нет,то берётся строка после start. Если кусок не найден, возвращается пустая строка.
     * Для поиска берётся строка до "\n\n" или "\r\n\r\n", если таковые присутствуют.
     */
    protected static String extract(String str, String start, String end)
    {
        int s = str.indexOf("\n\n", 0), e;
        if(s < 0) s = str.indexOf("\r\n\r\n", 0);
        if(s > 0) str = str.substring(0, s);
        s = str.indexOf(start, 0)+start.length();
        if(s < start.length()) return "";
        e = str.indexOf(end, s);
        if(e < 0) e = str.length();
        return (str.substring(s, e)).trim();
    }
}
