/**
 * Created by Sergo on 17.10.2016.
 */

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;


public class ClientsCreator extends UntypedActor {

    private LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    static class StartCreating {
        private Integer rCount;
        private Integer wCount;
        private Integer idFrom;
        private Integer idTill;

        public StartCreating(Integer rCount, Integer wCount, Integer idFrom, Integer idTill){
            this.rCount = rCount;
            this.wCount = wCount;
            this.idFrom = idFrom;
            this.idTill = idTill;
        }

        @Override
        public String toString() {
            return "Process Creator {" +
                    "rCount =" + rCount + ", wCount =" + wCount + "}";
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StartCreating) {
            log.info("Received {}", message);
            startCreating((StartCreating)message);
        }
        else unhandled(message);
    }

    private void startCreating(StartCreating message) {
        log.info("Started 1-st Reader {}", message);
        ActorRef firstReader = getContext().actorOf(Props.create(HttpClientReader.class));
        firstReader.tell(new HttpClientReader.StartProcess(message.rCount,message.idFrom,message.idTill), getSelf());

        log.info("Started 1-st Writer {}", message);
        ActorRef firstWriter = getContext().actorOf(Props.create(HttpClientWriter.class));
        firstWriter.tell(new HttpClientWriter.StartProcess(message.wCount,message.idFrom,message.idTill), getSelf());
    }
}
