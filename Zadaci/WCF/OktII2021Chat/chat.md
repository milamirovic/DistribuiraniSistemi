```csharp
namespace WcfChat
{
    [ServiceContract(CallbackContract = typeof(IChatCallback), SessionMode = SessionMode.Required)]
    public interface ICallback 
    {
        [OperationContract]
        public void Registracija(string nickname);

        [OperationContract(IsOneWay = true)]
        public void PosaljiPoruku(string primalac, string text);

        [OperationContract(IsOneWay = true)]
        public void MessageHistory(DateTime od, DateTime do);
    }

    [DataContract]
    public class Poruka
    {
        string text;
        string primalac;
        string posiljalac;
        DateTime vreme;

        [DataMember]
        public string Primalac { get; set;}

        [DataMember]
        public string Posiljalac { get; set;}

        [DataMember]
        public string Text { get; set;}

        [DataMember]
        public DateTime Vreme { get; set;}
    }
}

namespace WcfChat
{
    public interface IChatCallback
    {
        [OperationContract(IsOneWay = true)]
        public void PosaljiPorukuCallback(Poruka p);

        [OperationCOntract(IsOneWay = true)]
        public void MessageHistoryCallback(string zaPrikaz);
    }
}

namespace WcfChat.Data 
{
    public class Repository
    {
        private static Repository instance;
        private static object locker = true;

        public static Repository Instance
        {
            get 
            {
                if(locker)
                {
                    if(instance == null)
                    {
                        instance = new Repository();
                    }
                    return instance;
                }
            }
        }

        private List<Poruka> privatnePoruke;
        private List<Poruka> javnePoruke;

        public List<Poruka> JavnePoruke { get; set; }
        public List<Poruka> PrivatnePoruke { get; set; }

        protected Repository()
        {
            this.javnePoruke = new List<Poruka>();
            this.privatnePoruke = new List<Poruka>();
        }
    }
}

namespace WcfChat 
{
    [ServiceBehaviour(InstanceContentMode = InstanceContentMode.PerSession)]
    public class Chat : IChat
    {
        private string nickname;
        private Directory<string, IChatCallback> callbacks;

        public Chat()
        {
            callbacks = new Dictionary<string, IChatCallback>();
        }

        public void Registracija(string nickname)
        {
            if(this.callbacks.ContainsKey(nickname))
            {
                //pise ako vec postoji user sa tim nickname-om u sistemu smatrati da je 
                //prethodna sesija prestala da vazi, znaci radimo sledece:
                IChatCallback callback = this.callbacks[nickname];
                this.callbacks.Remove(nickname);
                this.callbacks.Add(nickname, callback);
                return;
            }
            //inace je nov user:
            this.nickname = nickname;
            var c = OperationContext.Current.GetCallbackChannel<IChatCallback>();
            this.callbacks.Add(nickname, c);
        }

        public Dictionary<string, IChatCallback> Callbacks
        {
            get
            {
                Registracija(this.nickname);
                return this.callbacks;
            }
        }

        public void PosaljiPoruku(string primalac, string text)
        {
            if(primalac == "SVI")
            {
                foreach(KeyValuePair kvp in this.callbacks)
                {
                    Poruka p = new Poruka()
                    {
                        Primalac = kvp.Key, Posiljalac = this.nickname, Text = text, Vreme = DateTime.Now()
                    };
                    kvp.Value.PosaljiPorukuCallback(p);
                    Repository.Instance.JavnePoruke.Add(p);
                }
            }
            else if(this.callbacks.ContainsKey(primalac))
            {
                Poruka p = new Poruka()
                {
                    Primalac = primalac, Posiljalac = this.nickname, Text = text, Vreme = DateTime.Now()
                };
                this.callbacks[primalac].PosaljiPorukuCallback(p);
                Repository.Instance.PrivatnePoruke.Add(p);
            }
        }

        public void MessageHistory(DateTime od, DateTime od)
        {
            //istorija primljenih poruka od tog do tog trenutka
            string zaPrikaz = "Istorija poruka za period od " + od.ToString() + " do " + do.ToString() + ": \n";
            foreach(Poruka p in Repository.Instance.JavnePoruke)
            {
                if(p.Primalac == this.nickname && p.Vreme >= od && p.Vreme <= do)
                {
                    zaPrikaz += "Posiljalac: " + p.Posiljalac + " Vreme slanja: " + p.Vreme.ToString() + " - poruka je namenjena svima.\n";
                }
            }
            foreach(Poruka p in Repository.Instance.PrivatnePoruke)
            {
                if(p.Primalac == this.nickname && p.Vreme >= od && p.Vreme <= do)
                {
                    zaPrikaz += "Posiljalac: " + p.Posiljalac + " Vreme slanja: " + p.Vreme.ToString() + " - poruka nije namenjena svima.\n";
                }
            }
            this.callbacks[this.nickname].MessageHistoryCallback(zaPrikaz);
        }
    }
}

namespace WcfChatClient
{
    public class Form : Form1, IChatCallback  
    {
        private ChatClient proxy;
        public Form1()
        {
            IntializeComponent();
            proxy = new ChatClient(new InstanceContext(this));
        }

        public void PosaljiPorukuCallback(Poruka p)
        {
            lblPrimalac.Text = p.Primalac;
            lblPosiljalac.Text = p.Posiljalac;
            lblText.Text = p.Text;
            lblVreme.Text = p.Vreme.ToString();
        }

        public void MessageHistoryCallback(string zaPrikaz)
        {
            lblMessageHistory.Text = zaPrikaz;
        }

        public void btnRegistracija_Click(object sender, EventArgs e)
        {
            string nickname = txtNickname.Text;
            proxy.Registracija(nickname);
            MessageBox.Show("Registracija obaveljena!");
        }

        public void btnPosaljiPoruku_Click(object sender, EventArgs e)
        {
            string primalac = txt primalac.Text;
            string text = txtText.Text;
            proxy.PosaljiPoruku(primalac, text);
        }

        public void btnMessageHistory(object sender, EventArgs e)
        {
            DateTime od = dateTimePicker1.Value;
            DateTime do = dateTimePicker2.Value;
            proxy.MessageHistory(od, do);
        }
    }
}
```
```html
<system.serviceModel>
    <services>
        <service name = "WcfChat.Chat">
            <endpoint 
            binding = "wsDualHttpBinding" 
            contract = "WcfChat.IChat"
            />
        </service>
    </services>
</system.serviceModel>
```
