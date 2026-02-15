using System;
using System.IO;

namespace FileSeek
{
    class Program
    {
        static string ConvName(string name)
        {
            string str = "";
            for (int i = 0; i < name.Length; i++)
            {
                char c = name[i];
                if (char.IsUpper(c) && i > 0 && !char.IsUpper(name[i - 1]))
                    str += "_";
                str += char.ToLower(c);
            }
            return str;
        }

        static void Main(string[] args)
        {
            if (args.Length != 1)
            {
                Console.WriteLine("usage: this.exe [dir]\n");
                return;
            }

            if (Directory.Exists(args[0]))
            {
                string dir = args[0];
                string outdir = dir + "\\" + "output";
                string[] files = Directory.GetFiles(dir);
                Directory.CreateDirectory(outdir);
                foreach (string file in files)
                {
                    string name = file.Split('\\')[file.Split('\\').Length - 1];
                    string ext = name.Split('.')[name.Split('.').Length - 1];
                    string id = name.Replace("." + ext, "");
                    Console.Write("{ \"");
                    Console.Write(id);
                    id = ConvName(id);
                    Console.Write("\", \"");
                    Console.Write(id);
                    Console.WriteLine("\" },");
                    File.Copy(file, outdir + "\\" + id + "." + ext, true);
                }
            }
        }
    }
}
