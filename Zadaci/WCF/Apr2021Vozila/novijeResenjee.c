namsepace WcfRegistracijaVozila
{
    [ServiceContract]
    public interface IRegistracijaVozila
    {
        [OperationContract]
        public void RegistrujVozilo(Vlasnik v, Vozilo voz);

        [OperationContract]
        public List<Vozilo> VratiVozilaVlasnika(Vlasnik v);

        [OperationContract]
        public List<Vlasnik> VratiVlasnikeModela(string model);

        [OperationContract]
        public Dictionary<Vozilo, Vlasnik> VratiSvaVozilaINjihoveVlasnike();
    }

    [DataContract]
    public class Vozilo 
    {
        int id;
        string marka;
        string model;
        string boja;

        [DataMember]
        public int Id { get; set; }

        [DataMember]
        public string Marka { get; set; }

        [DataMember]
        public string Model { get; set; }

        [DataMember]
        public string Boja { get; set; }

        public ovveride bool Equals(object obj)
        {
            return obj is Vozilo v && Id == v.Id;
        }
    }

    [DataContract]
    public class Vlasnik  
    {
        string ime;
        string prezime;
        string jmbg;

        [DataMember]
        public string Jmbg { get; set; }

        [DataMember]
        public string Ime { get; set  }

        [DataMember]
        public string Prezime { get; set; }
    }
}

namespace WcfRegistracijaVozila.Data 
{
    public class Repository 
    {
        private static Repository instance;
        private static object locker = true;

        public static Repoistory Instace 
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

        public Dictionary<int, Vlasnik> Baza { get; set; }
        public List<Vozilo> Vozila { get; set; }

        protected Repository()
        {
            Baza = new Dictionary<int, Vlasnik>();
            Vozila = new List<Vozilo>();
        }
    }
}

namespace WcfRegistracijaVozila
{
    [ServiceBehaviour(InstanceContextMode = InstanceCOntextMode.PerSession)]
    public class RegistracijaVozila : IRegistracijaVozila
    {
        public void RegistrujVozilo(Vlasnik v, Vozilo voz)
        {
            if(!Repository.Instance.Baza.ContainsKey(voz.Id))
            {
                Repository.Instance.Baza.Add(voz.Id, v);
                Repository.Instance.Vozila.Add(voz);
            }
            else
            {
                //to vozilo vec ima vlasnika
            }
        }

        public List<Vozilo> VratiVozilaVlasnika(Vlasnik v)
        {
            List<Vozilo> rez = new List<Vozilo>();
            if(!Repository.Instance.Baza.Values.Contains(v))
            {
                return rez;
            }
            else
            {
                foreach(KeyValuePair<int, Vlasnik> kvp in Repository.Instance.Baza)
                {
                    if(kvp.Value.Jmbg == v.Jmbg)
                    {
                        rez.Add(kvp.Key);
                    }
                }
                return rez;
            }
        }

        public List<Vlasnik> VratiVlasnikeModela(string model)
        {
            List<Vlasnik> rez = new List<Vlasnik>();
            foreach(Vozilo voz in Repository.Instance.Vozila)
            {
                if(voz.Model == model)
                {
                    rez.Add(Repository.Instance.Baza[voz.Id]);
                }
            }
            return rez;
        } 

        public Dictionary<Vozilo, Vlasnik> VratiSvaVozilaINjihoveVlasnike()
        {
            Dictionary<Vozilo, Vlasnik> rez = new Dictionary<Vozilo, Vlasnik>();
            foreach(Vozilo voz in Repository.Instance.Vozila)
            {
                rez.Add(voz, Repository.Instance.Baza[voz.Id]);
            }
            return rez;
        }
    }
}

namespace WcfRegistracijaVozila.Klijent
{
    public class Form1:Form
    {
        RegistracijaVozilaClient proxy;

        public Form1()
        {
            IntitalizeComponent();
            proxy = new RegistracijaVozilaClient();
        }

        private void btnRegistrujVozilo(object sender, EventArgs e)
        {
            string ime = txtIme.Text;
            string prez = txtPrez.Text;
            string jmbg = txtJmbg.Text;

            string marka = txtMarka.Text;
            string model = txtModel.Text;
            string boja = txtBoja.Text;
            int id = Int32.Parse(numericUpDown.Value);

            proxy.RegistrujVozilo(new Vlasnik
            {
                Ime = ime, Prezime = prez, Jmbg = jmbg
            }, 
            new Vozilo
            {
                Id = id, Marka = marka, Model = model, Boja = boja
            });

            MessageBox.Show("Registracija obavljena!");
        }

        private void btnVratiVozilaVlasnika(object sender, EventArgs e)
        {
            string ime, prezime, jmbg;
            ime = txtIme.Text;
            prezime = txtPrezime.Text;
            jmbg = txtJmbg.Text;
            List<Voilo> rez = proxy.VratiVozilaVlasnika(new Vlasnik
            {
                Ime = ime, Prezime = prezime, Jmbg = jmbg
            });

            string zaPrikaz = "";
            foreach(Vozilo v in rez)
            {
                zaPrikaz += "Vozilo [" + v.Id + "] marke " + v.Marka + ", modela " + v.Model + ", boje " + v.Boja + "\n";
            }

            lblPrikaz.Text = zaPrikaz;
        }

        private void btnVratiVlasnikeModela(object sender, EventArgs e)
        {
            string model = txtModel.Text;
            List<Vlasnik> rez = proxy.VratiVlasnikeModela(model);
            if(rez.Count==0)
            {
                MessageBox.Show("Nema vlasnika tog modela!");
            }
            else
            {
                string zaPrikaz = "";
                foreach(Vlasnik v in rez)
                {
                    zaPrikaz += v.Jmbg + " " + v.Ime + " " + v.Prezime + "\n";
                }
                MessageBox.Show(zaPrikaz);
            }
        }

        private btnVratiSvaVozilaINjihoveVlasnike(object sender, EventArgs e)
        {
            Dictionary<Vozilo, Vlasnik> rez = proxy.VratiSvaVozilaINjihoveVlasnike();
            if(rez.Count == 0)
            {
                MessageBox.Show("Nema vozila u bazi!");
            }
            else
            {
                string zaPrikaz = "";

                foreach(KeyValuePair<Vozilo, Vlasnik> kvp in rez)
                {
                    zaPrikaz += "Vozilo: " + kvp.Key.Id + " - " + kvp.Key.Marka + " - " + kvp.Key.Model + " - " + kvp.Key.Boja + "; Vlasnik: " + kvp.Value.Jmbg + " - " + kvp.Value.Ime + " " + kvp.Value.Prezime + "\n";
                }

                MessageBox.Show(zaPrikaz);
            }
        }
    }
}

//ILI AKO NE ZELIMO FORMU: 

namespace WcfRegistracijaVozila.Klijent
{
    class Program
    {
        static void main(String[] args)
        {
            RegistracijaVozilaClient proxy = new RegistracijaVozilaClient();

            while(true)
            {
                Console.WriteLine("Odaberite jednu od opcija:");
                Console.WriteLine("1 - Registracija vozila");
                Console.WriteLine("2 - Vrati listu vozila zadatog vlasnika");
                Console.WriteLine("3 - Vrati listu svih vlasnika datog modela");
                Console.WriteLine("4 - Vrati listu svih vozila i njihovih vlasnika");
                Console.WriteLine("5 - Izlaz")

                string odg = Console.ReadLine();
                if(odg=="5")
                {
                    break;
                }
                else if(odg == "1")
                {
                    string ime, prezime, jmbg, marka, model, boja;
                    Console.WriteLine("Unesite ime, prezime i jmbg vlasnika (svako u novom redu):");
                    ime = Console.ReadLine();
                    prezime = Console.ReadLine();
                    jmbg = Console.ReadLine();

                    Console.WriteLine("Unesite id, marku, model i boju vozila: ");
                    int id = Int32.Parse(Console.ReadILine());
                    marka = Console.ReadLine();
                    model = Console.ReadLine();
                    boja = Console.ReadLine();

                    proxy.RegistrujVozilo(new Vlasnik
                    {
                        Ime = ime, Prezime = prezime, Jmbg = jmbg
                    }, 
                    new Vozilo
                    {
                        Id = id, Marka = marka, Model = model, Boja = boja
                    });
                }
                else if(odg == "2")
                {
                    string ime, prezime, jmbg;
                    Console.WriteLine("Unesite ime, prezime i jmbg vlasnika, jedno ispod drugog:");
                    ime = Console.ReadLine();
                    prezime = Console.ReadLine();
                    jmbg = Console.ReadLine();

                    List<Vozilo> rez = proxy.vratiVozilaVlasnika(new Vlasnik
                    {
                        Ime = ime, Prezime = prezime, Jmbg = jmbg
                    });

                    foreach(Vozilo v in rez)
                    {
                        Console.WriteLine("Vozilo [" + v.Id + "] marka: " + v.Marka + ", model: " + v.Model + ", boja: " + v.Boja);
                    }
                }
                else if(odg == "3")
                {
                    Console.WriteLine("Unesite naziv marke vozila:");
                    string marka = Console.ReadLine();
                    List<Vlasnik> rez = proxy.VratiVlasnikeModela(model);
                    foreach(Vlasnik v in rez)
                    {
                        Console.WriteLine("Vlasnik[" + v.Jmbg + "], " + v.Ime + " " + v.Prezime);
                    }
                }
                else if(odg == "4")
                {
                    Dictionary<Vozilo, Vlasnik> rez = proxy.VratiSvaVozilaINjihoveVlasnike();
                    foreach(KeyValuePair<Vozilo, Vlasnik> kvp in rez)
                    {
                        Console.WriteLine("Vozilo: " + kvp.Key.Id + " - " + kvp.Key.Marka + " - " + kvp.Key.Model + " - " + kvp.Key.Boja + "; Vlasnik: " + kvp.Value.Jmbg + " - " + kvp.Value.Ime + " " + kvp.Value.Prezime);
                    }
                }
            }

        }
    }
}
