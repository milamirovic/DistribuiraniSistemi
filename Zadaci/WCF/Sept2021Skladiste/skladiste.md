```xml
<system.serviceModel>
    <services>
        <service name = "WcfZakupSkladista.ZakupSkladista">
            <endpoint
            binding = "basicHttpBinding"
            contract = "WcfZakupSkladista.IZakupSkladista"
            address = ""
            />
        </service>
    </services>
</system.serviceModel>
```

```csharp
namespace WcfZakupSkladista
{
    [ServiceContract]
    public interface IZakupSkladista
    {
        [OperationContract]
        public void Zakupi(Vlasnik vlasnik, Skladiste skladiste);

        [OperationContract]
        public List<Skladiste> VratiSkladistaVlasnika(Vlasnik v);

        [OperationContract]
        public List<Vlasnik> VratiVlasnikeAktivnihSkladista();

        [OperationContract]
        public Dictionary<int, string> VratiIstorijuVlasnikaSvihSkladista();
    }

    [DataContract]
    public class Vlasnik
    {
        string ime;
        string prezime;
        string jmbg;
        [DataMember]
        public string Ime 
        { 
            get { return this.ime; }
            set { this.ime = value; }
        }

        [DataMember]
        public string Prezime
        {
            get { return this.prezime; }
            set {this.prezime = value; }
        }

        [DataMember]
        public string Jmbg 
        {
            get { return this.jmbg; }
            set { this.jmbg = value; }
        }
    }

    [DataContract]
    public class Skladiste
    {
        int idSkladista;
        DateTime pocetakZakupa;
        DateTime krajZakupa;
        decimal cena;

        [DataMember]
        public int IdSkladista
        {
            get; set;
        }

        [DataMember]
        public DateTime PocetakZakupa
        {
            get;set;
        }

        [DataMember]
        public DateTime KrajZakupa
        {
            get;set;
        }

        [DataMember]
        public decimal Cena
        {
            get;set;
        }
    }
}

namespace WcfZakupSkladista
{
    [ServiceBehaviour(InstanceContextMode = InstanceContextMode.PerSession)]
    public class ZakupSkladista : IZakupSkladista
    {
        public void Zakupi(Vlasnik vlasnik, Skladiste skladiste)
        {
            //trazimo u BaziVlasnika key vlasnik
            if(Repository.BazaVlasnika.ContainsKey(vlasnik))
            {
                //okej, da li ovaj vlasnik vec ima dato skladiste u listi?
                if(Repository.BazaVlasnika[vlasnik].Contains(skladiste))
                {
                    //vec ima to skladiste, ne radimo nista
                }
                else
                {
                    Repository.BazaVlasnika[vlasnik].Add(skladiste);
                }
            }
            else
            {
                //novi vlasnik
                List<Skladiste> lista = new List<Skladiste>();
                lista.Add(skladiste);
                Repository.BazaVlasnika.Add(vlasnik, lista);
            }

            //trazimo to skladiste u BaziSkladista
            if(Repository.BazaSkladista.ContainsKey(skladiste))
            {
                //imamo to skladiste vec u bazi BaziSkladista
                //da li je vec zabelezen ovaj vlasnik kao vlasnik ovog skladista?
                if(Repository.BazaSkladista[skladiste].Contains(vlasnik))
                {
                    //vec ima tog vlasnika, nista ne radi
                }
                else
                {
                    //nema ga, dodajmo ga
                    Repository.BazaSkladista[skladiste].Add(vlasnik);
                }
            }
        }

        public List<int> VratiIdeveSkladistaDistinct()
        {
            List<int> ids = new List<int>();
            foreach(KeyValuePair<Skladiste, List<Vlasnik>> entry in Repository.BazaSkladista)
            {
                if(ids.Contains(entry.Key.IdSkladista))
                {
                    //vec smo ga upisali, nista
                }
                else 
                {
                    ids.Add(entry.Key.IdSkladista);
                }
            }
        }

        public List<int> VratiSkladistaVlasnika(Vlasnik v)
        {
            //vrati sva aktivna skladista vlasnika v
            //aktivna znaci da je danasnji datum manji od kraja zakupa
            List<int> ids = new List<int>();
            if(Repository.BazaVlasnika.ContainsKey(v))
            {
                //dakle taj vlasnik postoji
                List<Skladiste> lista = Repository.BazaVlasnika[v];
                for(int i=0;i<lista.Count();i++)
                {
                    if(lista[i].KrajZakupa > DateTime.Now && list[i].PocetakZakupa < DateTime.Now)
                    {
                        //onda je aktivno skladiste
                        if(ids.Contains(lista[i].IdSkladista))
                        {
                            //vec je zapamcen taj id
                        }
                        else 
                        {
                            ids.Add(lista[i].IdSkladista);
                        }
                    }
                }
            }
            //sad samo da budu distinct po id-u            
            return ids;
        }

        public List<Vlasnik> VratiVlasnikeAktivnihSkladista()
        {
            //vrati listu vlasnika svih aktivnih skladista 
            List<Vlasnik> lista = new List<Vlasnik>();

            foreach(KeyValuePair<Skladiste, List<Vlasnik>> entry in Repository.BazaSkladista)
            {
                // do something with entry.Value or entry.Key
                if(entry.Key.KrajZakupa > DateTime.Now && entry.Key.PocetakZakupa < DateTime.Now)
                {
                    //znaci aktivno je skladiste
                    for(int i=0;i<entry.Value.Count();i++)
                    {
                        lista.Add(entry.Value[i]);
                    }
                }
            }
            return lista.Select(x=>x).Distinct().ToList();//da se ne vracaju duplikati
        }

        public Dictionary<int, string> VratiIstorijuVlasnikaSvihSkladista()
        {
            List<int> ids = VratiIdeveSkladistaDistinct();
            Dictionary<int, string> rezultat = new Dictionary<Skladiste,List<String>>();
            for(int i=0;i<ids.Count();i++)
            {
                String istorija = ""; 
                foreach(KeyValuePair<Skladiste,List<Vlasnik>> entry in Repository.BazaSkladista)
                {
                    if(ids[i] == entry.Key.IdSkladista)
                    {
                        istorija += "Vlasnici skladista u periodu od " + entry.Key.PocetakZakupa.ToString() + " do " + entry.Key.KrajZakupa.ToString() + " su: \n";
                        List<Vlasnik> vlasnici = entry.Value;
                        foreach(Vlasnik v in vlasnici)
                        {
                            istorija += "- " + v.Ime.ToString() + " " + v.Prezime.ToString() + " " + v.Jmbg.ToString() + "\n";
                        }
                    }
                }
                rezultat.Add(ids[i], istorija);
            }
            return rezultat;
        }
    }
}

public class Repository 
{
    private static Repository instance;
    private static object locker = true;

    public static Repository Instance
    {
        get
        {
            lock(locker)
            {
                if(instance==null)
                {
                    instance = new Repository();
                }
            }
            return instance;
        }
    }

    //Dictionary<TKey, TValue>
    public Dictionary<Skladiste, List<Vlasnik>> BazaSkladista { get; set; }
    public Dictionary<Vlasnik, List<Skladiste>> BazaVlasnika { get; set; }

    protected Repository()
    {
        BazaSkladista = new Dictionary<Skladiste, List<Vlasnik>>();
        BazaVlasnika = new Dictionary<Vlasnik, List<Skladiste>>();
    }
}

namespace Klijent 
{
    public class Form1 : Form
    {
        ZakupSkladistaClient proxy;

        public Form1()
        {
            InitializeComponent();

            proxy = new ZakupSkladistaClient();
        }

        private void btnZakupi_Click(object sender, EventArgs e)
        {
            String ime, prezime, jmbg;
            ime = txtImeZaZakup.Text;
            prezime = txtPrezimeZaZakup.Text;
            jmbg = txtJmbgZaZakup.Text;

            int idSkl;
            if(int.TryParse(txtIdSkladista.Text, out idSkl))
            {
                //uspesno parsovanje
            }
            else 
            {
                MessageBox.Show("Pogresan unos id-a skladista!");
                return;
            }

            DateTime pocetak, kraj;
            pocetak = dtpPocetak.Value;
            kraj = dtpKraj.Value;

            decimal cena;
            if(decimal.TryParse(txtCena.Text, out cena))
            {
                //uspesno parsovanje
            }
            else 
            {
                MessageBox.Show("Pogresan unos cene skladista!");
                return;
            }

            //pokupljene su sve vrednosti
            //sad moze da se zakupi
            proxy.Zakupi(
                new Vlasnik({
                Ime = ime,
                Prezime = prezime,
                Jmbg = jmbg
                }),
                new Skladiste({
                    IdSkladista = idSkl,
                    PocetakZakupa = pocetak, 
                    KrajZakupa = kraj,
                    Cena = cena
                })
            );
        }

        private btnListaSvihAktivnihSkladistaVlasnika_Click(object sender, EventArgs e)
        {
            String ime = txtImeVlasnika1.Text;
            String prezime = txtPrezimeVlasnika1.Text;
            String jmbg = txtJmbgVlasnika1.Text;

            List<int> lista = proxy.VratiSkladistaVlasnika(new Vlasnik({
                Ime = ime,
                Prezime = prezime,
                Jmbg = jmbg
            }));

            String zaPrikaz = "";
            foreach(int s in lista)
            {
                zaPrikaz += "Skladiste " + s.ToString() + "\n";
            }

            lblListaSvihAktivinihSkladistaVlasnika.Text = zaPrikaz;
        }

        private btnListaSvihVlasnikaAktivnihSkladista_Click(object sender, EventArgs e)
        {
            List<Vlasnik> lista = proxy.VratiVlasnikeAktivnihSkladista();
            String zaPrikaz = "Lista vlasnika aktivnih skladista:\n";
            foreach(Vlasnik v in lista)
            {
                zaPrikaz += v.Jmbg + " : " v.Ime + " " + v.Prezime + "\n"; 
            }
            lblListaSvihVlasnikaAktivnihSkladista.Text = zaPrikaz;
        }

        private btnIstorija_Click(object sender, EventArgs e)
        {
            Dictionary<id, string> dict = proxy.VratiIstorijuVlasnikaSvihSkladista();
            String zaPrikaz = "Istorija vlasnistva svih skladista:\n";
            foreach(KeyValuePair<id, string> entry in dict)
            {
                zaPrikaz += "Skladiste " + entry.Key.ToString() + ":\n";
                zaPrikaz += entry.Value;
            }
        }
    }
}
```
