import javax.rmi.ssl.SslRMIClientSocketFactory;

public class Klijent
{
    private int id;
    private ArrayList<String> videoIgre;

    private Topic tId;
    private TopicPublisher tPublisherId;
    private TopicSubscriber tSubscriberId;

    private Queue qIdOdg;
    private QueueSender qSenderIdOdg;
    private QueueReceiver qReceiverIdOdg;

    private Queue qDownload;
    private QueueSender qSenderDownload;
    private QueueReceiver qReceiverDownload;

    private Queue qDownloadOdg;
    private QueueSender qSenderDownloadOdg;
    private QueueReceiver qReceiverDownloadOdg;

    private QueueConnection qc;
    private QueueSession qs;
    private TopicConnection tc;
    private TopicSession ts;

    public Klijent(int  id, ArrayList<String> videoIgre) 
    {
        this.id = id;
        this.videoIgre = videoIgre;

        InitialContext ictx = new InitialContext();

        TopicConnectionFactory tcf = (TopicConnectionFactory)ictx.lookup("tcfVideoIgre");
        QueueConnectionFactory qcf = (QueueConnectionFactory)ictx.lookup("qcfVIdeoIgre");
        tId = (Topic)ictx.lookup("tId");
        qIdOdg = (Queue)ictx.lookup("qIdOdg");
        qDownload = (Queue)ictx.lookup("qDownload");
        qDownloadOdg = (Queue)ictx.lookup("qDownloadOdg");

        ictx.close();

        tc = (TopicConnection)tcf.createTopicConnection();
        ts = (TopicSession)tc.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);

        qc = (QueueConnection)qcf.createQueueConnection();
        qs = (QueueSession)qc.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);

        tc.start();
        qc.start();

        //svima salje svoj id
        tPublisherId = (TopicPublisher)ts.createPublisher(tId);
        Message msg = ts.createMessage();
        msg.setIntProperty("noviId", this.id);
        tPublisher.publish(msg);
        ts.commit();

        //svi primaju njegov id i odgovore mu svojim id-em
        tSubscriber = (TopicSubscriber)ts.createSubscriber(tId);
        qSenderIdOdg = (QueueSender)qs.createSender(qIdOdg);
        tSubscriber.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Novi klijent: " + msg.getIntProperty("noviId"));
                msg.setIntProperty("odgId", this.id);
                qSender.send(msg);
                qs.commit();
            }
        });

        //novi prima odgovore klijenata
        qReceiverIdOdg = (QueueReceiver)qs.createReceiver(qIdOdg, "noviId = " + this.id);
        qReceiverIdOdg.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Poydravlja Vas klijent: " + msg.getIntProperty("odgId"));
            }
        });

        //klijenti primaju eventualne zahteve download i daju odgovor
        qSenderDownloadOdg = (QueueSender)qs.createSender(qDownloadOdg);
        for(String v : this.videoIgrice)
        {
            qReceiverDownload = (QueueReceiver)qs.createReceiver(qDownload, "videoIgra = '" + v + "'");
            qReceiverDownload.setMessageListener(new MessageListener(){
                @Override
                public void onMessage(Message msg)
                {
                    System.out.println("Klijent " + msg.getIntProperty("idTrazi") + " Vam trazi video igru: " + msg.getStringProperty("videoIgra"));
                    msg.setIntProperty("idDaje", this.id);
                    qSenderDownloadOdg.send(msg);
                    qs.commit();
                }
            });
        }

        //onaj koji je trazio prima odgovor na zahtev download 
        qReceiverDownloadOdg = (QueueReceiver)qs.createReceiver(qDownloadOdg, "idTrazi = " + this.id);
        qReceiverDownloadOdg.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) 
            {
                System.out.println("Video igru " + msg.getStringProperty("videoIgra") + " Vam daje klijent: " + msg.getIntProperty("idDaje"));
            }
        });   
    }

    public void download(String videoGameName)
    {
        //trazi neku video igricu
        if(this.videoIgre.contains(videoGameName))
        {
            //vec ima tu video igricu 
            System.out.println("Vec posedujete navedenu video igru!");
            return;
        }
        qSenderDownload = (QueueSender)qs.createSender(dDownload);
        Message m = qs.createMessage();
        m.setIntProperty("idTrazi", this.id);
        m.setStringProperty("videoIgra", videoGameName);
        qSenderDownload.send(m);
        qs.commit();
    }

    public void zatvori()
    {
        tc.close();
        qc.close();
    }

    public static void main(String[] args) 
    {
        Scanner s = new Scanner(System.in);
        int id = getID();//fja koju ne implementiramo

        ArrayList<String> videoIgre = new ArrayList<String>();

        while(true)
        {
            System.out.println("Unesite naziv nove video igre: [za izlaz otkucati 'end']");
            String odg = s.nextLine();
            if(odg.equals("end") || odg.equals("END"))
            {
                break;
            }
            if(videoIgre.contains(odg))
            {
                System.out.println("Tu igru vec imate u svojoj listi video igara! probajte opet!");
            }
            else
            {
                videoIgre.add(odg);
            }
        }

        Klijent k = new Klijent(id, videoIgre);
        
        while(true)
        {
            System.out.println("Zelite li da potrazite neku video igru? (DA/NE)");
            String odg = s.nextLine();
            if(odg.equals("NE"))
            {
                break;
            }
            else
            {
                System.out.println("Unesite naziv igre koju zelite da pribavite: ");
                String igra = s.nextLine();
                k.downlaod(igra);
            }
        }

        k.zatvori();

        s.nextLine();
        s.nextLine();

        System.exit(0);
    }
}
