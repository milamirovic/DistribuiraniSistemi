```csharp
namespace WcfLudo
{
    [ServiceContract(CallbackContract = typeof(ILudoCallback), SessionMode = SessionMode.Required)]
    public interface ILudo
    {
        [OperationContract]
        public void Registracija(string nickname);

        [OperationContract(IsOneWay=true)]
        public void BaciKocku(string nickname);
    }

    [DataContract]
    public class Igra
    {
        List<Igrac> igraci;
        Igrac pobednik;
        int id;

        [DataMember]
        public List<Igrac> Igraci { get; set;}

        [DataMember]
        public Igrac Pobednik { get; set }

        [DataMember]
        public int Id { get; set }
    }

    [DataContract]
    public class Igrac 
    {
        string nickname;
        int x;
        int y;
        Igra igra;
        int pojeden = 0;
        int pojeo = 0;        

        [DataMember]
        public string Nickname { get; set; }

        [DataMember]
        public int X { get; set; }

        [DataMember]
        public int Y { get; set; }

        [DataMember]
        public Igra Igra { get; set;}

        [DataMember]
        public int Pojeden { get; set; }

        [DataMember]
        public int Pojeo { get; set; }
    }
}

namespace WcfLudo
{
    public interface ILudoCallback
    {
        [OeprationContract(IsOneWay=true)]
        public void PojedenCallback(Igrac i, Igrac pojeoGa);

        [OperationContract(IsOneWay=true)]
        public void PobedioCallback(Igrac i);

        [OperationContract(IsOneWay=true)]
        public void PromenaCallback(string poruka);
    }
}

namespace WcfLudo.Data  
{
    public class Repository 
    {
        public static Repository instance;
        public static object locker = true;

        public Repository Instance 
        {
            get 
            {
                if(locker)
                {
                    if(instance == null)
                    {
                        this.instance = new Repository();
                    }
                    return instance;
                }
            }
        }

        List<Igrac> igraci;

        public Dictionary<string, Igrac> Igraci { get; set; }

        protected Repository()
        {
            this.igraci = new Dictionary<string, Igrac>();
        }
    }
}

namespace WcfGame
{
    [ServiceBehaviour(InstanceContentMode = InstanceContentMode.Single)]
    public class Ludo : ILudo
    {
        private Dictionary<string, ILudoCallback> igraci;
        private Dictionary<int, Igra> igre;

        public Ludo()
        {
            this.igraci = new Dictionary<string, ILudoCallback>();
            this.igre = new Dictionary<int, Igra>();
        }

        public void Registracija(string nickname)
        {
            Igra igra = null;
            if(this.igraci.ContainsKey(nickname))
            {
                //vec postoji dati igrac, samo mu prosledi novu instancu igre
                Igrac igrac = Repository.Instance.Igraci[nickname];
                if(igrac.Igra.Pobednik == null)
                {
                    //onda treba da ostane u istoj igri
                    igra = igrac.Igra;
                    igra.Igraci.Remove(igrac);
                    igra.Igraci.Add(igrac);
                } 
                else 
                {
                    //ako je pobednik nadjen, igra je zavrsena, pa igrac onda hoce novu igru da igra 
                    foreach(Igra i in this.igre.Values)
                    {
                        if(i.Pobednik == null && i.Igraci.Count < 4)
                        {
                            igra = i;  
                            break;
                        }
                    }
                    if(igra!=null)
                    {
                        igrac.Igra = igra; 
                        igra.Igraci.Add(igra);
                        foreach(Igrac igr in igra.Igraci)
                        {
                            this.igraci[igr.Nickname].PromenaCallback("U igru je dodat igrac " + igrac.Nickname + " na poziciji X:" + igrac.X + ", Y: " + igrac.Y + "\n");
                        }
                    }
                    else 
                    {
                        igra = new Igra() 
                        {
                            Igraci = new List<Igrac>(), Pobednik = null, Id = this.igre.Count
                        };
                        igrac.Igra = igra;
                        igra.Igraci.Add(igra);
                    }
                }
            }
            else 
            {
                //igrac je potpuno nov
                Igrac igrac = new Igrac()
                {
                    Nickname = nickname, X = SlucajnoX(), Y = SlucajnoY(), Igra = null, Pojeo = 0, Pojeden = 0
                };

                foreach(Igra i in this.igre.Values)
                {
                    if(i.Pobednik == null && i.Igraci.Count < 4)
                    {
                        igra = i;
                        break;
                    }
                }

                if(igra == null)
                {
                    //nije nadjena igra koja ima mesta za novog igraca
                    //moramo da napravimo novu igru

                    igra = new Igra() 
                    {
                        Igraci = new List<Igrac>(), Pobednik = null, Id = this.igre.Count
                    };
                    igrac.Igra = igra;
                    igra.Igraci.Add(igrac);
                    var c = OperationContext.Current.GetCallbackChannel<ILudoCallback>();
                    this.igraci.Add(nickname, c);
                    Repository.Instance.Igraci.Add(nickname, igrac);
                }
                else
                {
                    igrac.Igra = igra;  
                    igra.Igraci.Add(igrac);
                    var c = OperationCOntext.Current.GetCallbackChannel<ILudoCallback>();
                    this.igraci.Add(nickname, c);
                    Repository.Instance.Igraci.Add(nickname, igrac);
                }

                foreach(Igrac igr in igra.Igraci)
                {
                    this.igraci[igr.Nickname].PromenaCallback("U igru je dodat igrac " + igrac.Nickname + " na poziciji X:" + igrac.X + ", Y: " + igrac.Y + "\n");
                }
            }
        }

        public void BaciKocku(string nickname)
        {
            if(!Repository.Instance.Igraci.ContainsKey(nickname) && !this.igraci.ContainsKey(nickname))
            {
                //igrac je nepostojeci!
                return;
            }

            Igrac igrac = Repository.Instance.Igraci[nickname];
            Igra igra = igrac.Igra;
            if(igra.Pobedio != null)
            {
                //ova igra je gotova
                return;
            }
            int broj = kockica(6);//broj od 1 do 6
            igrac.X += broj;
            igrac.Y += broj;
            
            foreach(Igrac i in igra.Igraci)
            {
                if(i.X == igrac.X && i.Y == igrac.Y)
                {
                    //znaci da je ovaj igrac upravo pojeo igraca i 
                    this.igraci[i.Nickname].PojedenCallback(i, igrac);
                    igrac.Pojeo++;
                    i.Pojeden++;
                    //posto je pojeden dobija novu lokaciju:
                    i.X = slucajnoX();
                    i.Y = slucajnoY();
                    foreach(Igrac igr in igra.Igraci)
                    {
                        this.igraci[igr.Nickname].PromenaCallback("Igrac " + igrac.Nickname + " je pojeo igraca:" + i.Nickname);
                    }
                }
            }

            foreach(Igrac i in igra.Igraci)
            {
                if(i.Pojeo==10)
                {
                    igra.Pobednik = i;
                    this.igraci[i.Nickname].PobedioCallback(i);
                    foreach(Igrac igr in igra.Igraci)
                    {
                        this.igraci[igr.Nickname].PromenaCallback("Pobedio je igrac " + i.Nickname);
                    }
                    break;
                }
            }
        }
    }
}

namespace WcfLudo.Client
{
    public class Form1 : Form, ILudoCallback
    {
        private LudoClient proxy;

        public Form1()
        {
            InitializeComponent();
            proxy = new LudoClient(new InstanceContext(this));
        }

        public void PobedioCallback(Igrac i)
        {
            MessageBox.Show("Kraj igre! Cestitamo,  " + i.Nickname + " pobedili ste!");
        }

        public void PojedenCallback(Igrac i, Igrac pojeoGa)
        {
            MessageBox.Show(i.Nickname + ", pojedeni ste od strane igraca: " + pojeoGa.Nickname);
        }

        public void PromenaCallback(string poruka)
        {
            MessageBox.Show("PROMENA: " + poruka);
        }

        public void btnRegistracija_Click(object sender, EventArgs e)
        {
            string nickname = txtNickname.Text;
            proxy.Registracija(nickname);
        }

        public void btnBaciKockicu_Click(object sender, EventArgs e)
        {
            string nickname = txtNickname.Text;
            proxy.BaciKockicu(nickname);
        }
    }
}
```
```html
<system.serviceModel>
    <services>
        <service name = "WcfLudo.Ludo">
            <endpoint 
            binding = "wsDualHttpBinding"
            contract = "WcfLudo.ILudo"
            />
        </service>
    </services>
</system.serviceModel>>
```
