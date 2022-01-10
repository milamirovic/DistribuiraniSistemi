```csharp
namespace WcfRegistracijaVozila
{
    [ServiceContract]
    public interface IRegistracijaVozila
    {
        [OperationContract]
        public void RegistrujVozilo(Vlasnik v, Vozilo a);

        [OperationContract]
        public List<Vozilo> VozilaVlasnika(Vlasnik v);

        [OperationContract]
        public List<Vlasnik> VlasniciModela(string model);

        [OperationContract]
        public Dictionary<Vozilo, Vlasnik> SvaVozilaIVlasnici();
    }

    [DataContract]
    public class Vlasnik 
    {
        string ime;
        string prezime;
        string jmbg;

        [DataMember]
        public string Ime { get; set; }

        [DataMember]
        public string Prezime { get; set;}

        [DataMember]
        public string Jmbg { get; set; }
    }

    [DataContract]
    public class Vozilo 
    {
        string marka;
        string model;
        string boja;

        [DataMember]
        public string Marka { get; set; }

        [DataMember]
        public string Model { get; set; }

        [DataMember]
        public string Boja { get; set; }
    }
}

namespace WcfRegistracijaVozila.Data
{ 
    public class Repository 
    {
        private Repository instance;
        private static object locker = true;

        public static Repository Instance
        {
            get
            {
                if(instance == null)
                {
                    instance = new Repository();
                }
                return instance;
            }
        }

        public Dictionary<string, List<Vozilo>> Baza;

        protected Repository()
        {
            Baza = new Dictionary<string, List<Vozilo>>();
        }
    }
}

namespace WcfRegistracijaVozila
{
    [ServiceBehaviour(InstanceContextMode = InstanceContextMode.Single)]
    public class RegistracijaVozila : IRegistracijaVozila
    {
        public RegistracijaVozila() { }

        public void RegistrujVozilo(Vlasnik v, Vozilo a)
        {
            if(!Repository.Instance.Baza.ContainsKey(v.Jmbg))
            {
                Repository.Instance.Baza.Add(v.Jmbg, a);
            }
            else
            {
                //jedan vlasnik sme da ima vise autobila iste marke, modela i boje, koliko ja znam :)
                Repository.Instance.Baza[v.Jmbg].Add(a);
            }
        }

        public List<Vozilo> VozilaVlasnika(Vlasnik v)
        {
            if(!Repository.Instance.Baza.ContainsKey(v.Jmbg))
                return null;

            List<Vozilo> rez = new List<Vozilo>();
            foreach(Vozilo a in Repository.Instance.Baza[v.Jmbg])
            {
                rez.Add(a);
            }

            return rez;
        }

        public List<Vlasnik> VlasniciModela(string model)
        {
            List<Vlasnik> rez = new List<Vlasnik>();

            foreach(KeyValuePair<string,List<Vozilo>> kvp in Repository.Instance.Baza)
            {
                foreach(Vozilo v in kvp.Value)
                {
                    if(v.Model == model)
                    {
                        rez.Add(kvp.Key);
                        break;
                    }
                }
            }

            return rez;
        }

        public Dictionary<Vozilo, Vlasnik> SvaVozilaIVlasnici()
        {
            Dictionary<Vozilo, Vlasnik> rez = new Dictionary<Vozilo,Vlasnik>();

            foreach(KeyValuePair<string, List<Vozilo>> kvp in Repository.Instance.Baza)
            {
                foreach(Vozilo v in kvp.Value)
                {
                    rez.Add(v, kvp.Key);
                }
            }

            return rez;
        }
    }
}

namespace KlijentBezForme
{
    public class Program
    {
        static void Main(string[] args)
        {
            RegistracijaVozilaClient proxy = new RegistracijaVozilaClient();

            while(true)
            {
                Console.WriteLine("Odaberite opciju:");
                Console.WriteLine("1 - Registruj vozilo");
                Console.WriteLine("2 - Pribavi vozila vlasnika");
                Console.WriteLine("3 - Pribavi vlasnike modela");
                Console.WriteLine("4 - Pribavi sva vozila i vlasnike");
                Console.WriteLine("5 - Izlaz")

                if(odg == "5")
                {
                    break;
                }
                else if(odg == "1")
                {
                    Console.WriteLine("Unesite svoje ime:");
                    String ime = Console.ReadLine();

                    Console.WriteLine("Unesite svoje prezime:");
                    String prezime = Console.ReadLine();

                    Console.WriteLine("Unesite svoj JMBG: ");
                    String jmbg = Console.ReadLine();

                    Console.WriteLine("Unesite marku vozila:");
                    String marka = Console.ReadLine();

                    Console.WriteLine("Unesite model vozila:");
                    String model = Console.ReadLine();

                    Console.WriteLine("Unesite boju vozila:");
                    String boja = Console.ReadLine();

                    proxy.RegistrujVozilo(new Vlasnik()
                    {
                        Ime = ime, Prezime = prezime, Jmbg = jmbg
                    }, new Vozilo()
                    {
                        Marka = marka, Model = model, Boja = boja
                    });
                }
                else if(odg == "2")
                {
                    Console.WriteLine("Unesite ime vlasnika:");
                    String ime = Console.ReadLine();

                    Console.WriteLine("Unesite prezime vlasnika:");
                    String prezime = Console.ReadLine();

                    Console.WriteLine("Unesite Jmbg vlasnika:");
                    String jmbg = Console.ReadLine();

                    List<Vozila> rez = proxy.VozilaVlasnika(new Vlasnik()
                    {
                        Ime = ime, Prezime = prezime, Jmbg = jmbg
                    });
                    Console.WriteLine("Vozila vlasnika " + ime + " " + prezime  + " su:");
                    foreach(Vozilo v in rez)
                    {
                        Console.WriteLine("Vozilo - marka: " + v.Marka + " - model: " + v.Model + " - boja: " + v.Boja);
                    }
                }
                else if(odg == "3")
                {
                    Console.WriteLine("Unesite model: ");
                    String model = Console.ReadLine();
                    List<Vlasnik> rez = proxy.VlasniciModela(model);
                    Console.WriteLine("Vlasnici modela " + model + " su:");
                    foreach(Vlasnik v in rez)
                    {
                        Console.WriteLine(v.Ime + " " + v.Prezime + " " + v.Jmbg);
                    }
                }
                else if(odg == "4")
                {
                    Dictionary<Vozilo, Vlasnik> rez = proxy.SvaVozilaIVlasnici();
                    foreach(KeyValuePair<Vozilo, Vlasnik> kvp in rez)
                    {
                        Console.WriteLine("Vozilo [" + kvp.Key.Marka + ", " + kvp.Value.Model + ", " + kvp.Key.Boja "] - Vlasnik [" + kvp.Value.Ime + ", " + kvp.Value.Prezime + ", " + kvp.Value.Jmbg + "]");
                    }
                }
                else
                {
                    Console.WriteLine("Pogresan unos!");
                }

            }

            Console.ReadLine();

        }
    }
}

namespace KlijentForm
{
    public class Form1 : Form
    {
        private RegistracijaVozilaClient proxy = new RegistracijaVozilaClient();

        public Form1()
        {
            InitializeComponent();
        }

        public void btnRegistrujVozilo(object sender, EventArgs e)
        {
            string ime = txtIme.Text;
            string prezime = txtPrezime.Text;
            string jmbg = txtJmbg.Text;

            string marka = txtMarka.Text;
            string model = txtModel.Text;
            string boja = txtBoja.Text;

            proxy.RegistrujVozilo(new Vlasnik()
            {
                Ime = ime, Prezime = prezime, Jmbg = jmbg
            }, new Vozilo()
            {
                Marka = marka, Model = model, Boja = boja
            });
        }

        public void btnVozilaVlasnika(object sender, EventArgs e)
        {
            string ime = txtIme2.Text;
            string prezime = txtPrezime2.Text;
            string jmbg = txtJmbg2.Text;

            List<Vozilo> rez = proxy.VozilaVlasnika(new Vlasnik()
            {
                Ime = ime, Prezime = prezime, Jmbg = jmbg
            });

            String zaPrikaz = "Vozila datog vlasnika su:\n";

            foreach(Vozilo v in rez)
            {
                zaPrikaz += v.Marka + " " + v.Model + " " + v.Boja + "\n";
            }

            lblZaPrikaz.Text = zaPrikaz;
        }

        public void btnVlasniciModela(object sender, EventArgs e)
        {
            string model = txtModel2.Text;
            List<Vlasnik> rez = proxy.VlasniciModela(model);
            String prikaz = "Vlasnici datog modela su:\n";
            foreach(Vlasnik v in rez)
            {
                prikaz += v.Ime + " " + v.Prezime + " " + v.Jmbg + "\n";
            }
            lblPrikaz2.Text = prikaz;
        }

        public btnSvaVozilaIVlasnici(object sender, EventArgs e)
        {
            Dictionary<Vozilo, Vlasnik> rez = proxy.SvaVozilaIVlasnici();
            string prikazz = "Sva vozila i njihovi vlasnici:\n";
            foreach(KeyValuePair<Vozilo, Vlasnik> kvp in rez)
            {
                prikaz += "Vozilo[" + kvp.Key.Marka + ", " + kvp.Key.Model + ", " + kvp.Key.Boja + "] - Vlasnik [" + kvp.Value.Ime + ", " + kvp.Value.Prezime + ", " + kvp.Value.Jmbg + "]\n";
            }
        }
    }
}


```
```xml
<system.serviceModel>
    <services>
        <service name="WcfRegistracijaVozila.RegistracijaVozila>
            <endpoint
            binding = "basicHttpBinding"
            contract = "WcfRegistracijaVozila.IRegistracijaVozila"
            />
        </service>
    </services>
</system.serviceModel>
```
