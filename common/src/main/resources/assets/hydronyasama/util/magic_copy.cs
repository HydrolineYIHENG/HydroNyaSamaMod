using System;
using System.IO;

namespace MagicCopy
{
    class Program
    {
        const string REPLACE_TAG = "#replace";

        static void FileToDirectory(string file, string indir, string outdir)
        {
            string name = file.Split('\\')[file.Split('\\').Length - 1];
            string ext = name.Split('.')[name.Split('.').Length - 1];
            name = name.Replace("." + ext, "");

            if (!Directory.Exists(outdir))
                Directory.CreateDirectory(outdir);

            string content = File.ReadAllText(file);
            string[] infiles = Directory.GetFiles(indir);
            foreach (string s in infiles)
            {
                var tmp = s.Split('\\')[s.Split('\\').Length - 1];
                var tmpext = tmp.Split('.')[tmp.Split('.').Length - 1];
                string id = tmp.Replace("." + tmpext, "");
                Console.WriteLine("working on: " + id);
                var data = content.Replace(REPLACE_TAG, id);
                if (name.Contains(REPLACE_TAG))
                    id = name.Replace(REPLACE_TAG, id);
                File.WriteAllText(outdir + "\\" + id + "." + ext, data);
            }
        }

        static void Main(string[] args)
        {
            if (args.Length != 3)
            {
                Console.WriteLine("usage: this.exe [file/dir] [ref dir] [out dir]\n");
                return;
            }
            
            if (File.Exists(args[0]))
            {
                string file = args[0];
                string indir = args[1];
                string outdir = args[2];

                FileToDirectory(file, indir, outdir);
            }
            else if (Directory.Exists(args[0]))
            {
                string indir = args[0];
                string refdir = args[1];
                string outdir = args[2];

                string[] infiles = Directory.GetFiles(indir);
                foreach (string file in infiles)
                {
                    FileToDirectory(file, refdir, outdir);
                }
            }

            Console.WriteLine();
            Console.WriteLine("press any key to exit.");
            Console.ReadKey(true);
            Console.WriteLine();
        }
    }
}
