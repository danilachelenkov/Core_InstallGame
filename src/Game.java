import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Game {
    private String pathToInstall;
    private final StringBuilder stringBuilder;
    private File fileLog;
    private int countGamers;
    private List<GameProgress> listGamers;
    private List<GameProgress> listDeserializationGamers;

    public List<GameProgress> getListDeserializationGamers() {
        return listDeserializationGamers;
    }

    public Game(int countGamers, String pathToInstall) {
        this.stringBuilder = new StringBuilder();
        this.countGamers = countGamers;
        this.listGamers = new ArrayList<>();
        this.pathToInstall = pathToInstall + "\\Games";

        checkInstallGame();
    }

    private boolean checkInstallGame() {
        File fileInstallGameCheck = new File(pathToInstall + "//gameIsInstalled");
        fileLog = new File(pathToInstall + "//temp//temp.txt");

        if (fileInstallGameCheck.exists()) {
            if (fileLog.exists()) {
                return true;
            }
        }
        return false;
    }

    public void start(String[] nickname) {
        for (int i = 0; i < countGamers; i++) {
            listGamers.add(new GameProgress(
                    nickname[i],
                    new Random().nextInt(101),
                    new Random().nextInt(100),
                    new Random().nextInt(10),
                    new Random().nextDouble(100)));
        }
        saveGame("", listGamers);
    }

    /**
     * Метод сериализации классов для сохраненных параметров прогресса игры каждого игрока
     *
     * @param path       - путь каталога в который будет выполняться сохранение сериализованного класса прогресса
     * @param listGamers - перечень объектов програсса для каждого игрока
     * @return - возращает успешно или не успешно был выполнен метод по переданному списку
     */
    private boolean saveGame(String path, List<GameProgress> listGamers) {
        if (path.isEmpty()) {
            path = pathToInstall + "\\savegame";
        }

        FileOutputStream fos;
        ObjectOutputStream oos;

        try {
            for (GameProgress progress : listGamers) {

                fos = new FileOutputStream(path + "//pg_" + progress.getNickname() + ".dat");
                oos = new ObjectOutputStream(fos);
                oos.writeObject(progress);

                fos.close();
                oos.close();
            }

        } catch (FileNotFoundException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
            return false;
        } catch (IOException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
            return false;
        }

        return true;
    }

    /**
     * Метод распаковывает архив и выполняет десириализацию в поле экземпляра класса
     *
     * @param pathToArchFile - пусть к файлу с архивом
     * @param extractPath    - путь к катологу для извлечения туда файлов архива
     * @return - возвращает состояние процесса успех\не успех
     */
    public boolean extractArchive(String pathToArchFile, String extractPath) {

        if (extractPath.equals("")) {
            extractPath = pathToInstall + "\\savegame\\archive";
        }

        if (pathToArchFile.equals("")) {
            return false;
        }

        unpackArchive(pathToArchFile, extractPath);
        listDeserializationGamers = deserializationFile(extractPath);
        return true;
    }

    /**
     * Метод возвращает путь, по которому была выполнена разархивация файлов
     *
     * @param pathToArchFile - путь к файлу с архивом для разархивации
     * @param extractPath    - путь к каталогу для разархивации в который будут сохранены файлы
     * @return возвращает путь к каталок
     */
    private boolean unpackArchive(String pathToArchFile, String extractPath) {

        File folderToUnpack = new File(extractPath);
        if (folderToUnpack.exists()) {
            for (File file : folderToUnpack.listFiles()) {
                file.delete();
            }
        } else {
            folderToUnpack.mkdir();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(pathToArchFile))) {

            String nameEntry;
            ZipEntry zipEntry;
            long size;

            while ((zipEntry = zis.getNextEntry()) != null) {
                nameEntry = zipEntry.getName();
                size = zipEntry.getSize();

                FileOutputStream fileOutputStream = new FileOutputStream(extractPath + "\\" + nameEntry);
                for (int c = zis.read(); c != -1; c = zis.read()) {
                    fileOutputStream.write(c);
                }
                fileOutputStream.flush();
                zis.closeEntry();
                fileOutputStream.close();
            }

        } catch (IOException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
            return false;
        }

        return true;
    }

    private List<GameProgress> deserializationFile(String pathForDeser) {
        List<GameProgress> list = new ArrayList<GameProgress>();

        for (File deserFile : new File(pathForDeser).listFiles()) {

            try (FileInputStream fis = new FileInputStream(deserFile)) {
                ObjectInputStream ois = new ObjectInputStream(fis);
                list.add((GameProgress) ois.readObject());
            } catch (IOException e) {
                writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
            } catch (ClassNotFoundException e) {
                writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
            }
        }


        return list;
    }

    /**
     * Метод добавляет файлы из каталога в zip-архив.
     *
     * @param pathArchive       - путь по которому создается файл архива, в который будут добавляться файлы
     * @param pathSaveFolder    - каталог из которой будет выполняться архивирование файлов
     * @param deletePackedFiles - признак удаления всех файлов из каталога, который были заархивированы
     */
    public void addToArchive(String pathArchive, String pathSaveFolder, boolean deletePackedFiles) {
        if (pathArchive.isEmpty())
            pathArchive = pathToInstall + "\\savegame\\archive.zip";

        if (pathSaveFolder.isEmpty())
            pathSaveFolder = pathToInstall + "\\savegame";

        List<File> listFilesForDelete = new ArrayList<File>();

        ZipOutputStream zout;
        FileInputStream fis;
        ZipEntry entry;
        try {

            File saveFolder = new File(pathSaveFolder);

            File archFile = new File(pathArchive);
            if (archFile.exists()) {
                archFile.delete();
            }

            File archFolder = new File(pathSaveFolder + "\\archive");
            if (archFolder.exists()) {
                for (File file : archFolder.listFiles()) {
                    file.delete();
                }
                archFolder.delete();
            }

            zout = new ZipOutputStream(new FileOutputStream(pathArchive));

            if (saveFolder.isDirectory()) {

                for (File savefile : saveFolder.listFiles()) {

                    if (savefile.getName().substring(savefile.getName().lastIndexOf(".") + 1).equals("zip")) {
                        /*можно добавить массив или список во входящие, в котором будут перечислены все расширения для исключения
                          и проверять текущее расширение на вхождение в массиd или список
                        */
                        continue;
                    }

                    fis = new FileInputStream(savefile.getPath());
                    entry = new ZipEntry("pack_" + savefile.getName());
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);

                    zout.write(buffer);
                    zout.closeEntry();
                    fis.close();

                    listFilesForDelete.add(savefile);
                }

                zout.close();

                if (deletePackedFiles) {
                    deleteFileInFolder(listFilesForDelete);
                }
            }
        } catch (FileNotFoundException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
        } catch (IOException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
        }
    }

    /**
     * Метод выполняет удаление файлов по строго переданному списку
     *
     * @param listFiles - принимает на вход список файлов
     * @return - не возвращает значений
     */
    private void deleteFileInFolder(List<File> listFiles) {
        for (File file : listFiles) {
            file.delete();
        }
    }

    /**
     * Метод выполняет установку игры в указанную директорию
     *
     * @return - результат установки игры успех\не успех
     */
    public boolean install() {

        try {
            if (new File(pathToInstall + "//gameIsInstalled").exists()) {
                writeToLogFile(stringBuilder.append(String.format("%s Игра была установлена ранее\r\n", LocalDateTime.now())), fileLog);
                return true;
            }

            File fsoGame = createFolder(pathToInstall);

            File fsoScr = createFolder(fsoGame.getPath() + "//scr");
            File fsoRes = createFolder(fsoGame.getPath() + "//res");
            File fsoSaveGame = createFolder(fsoGame.getPath() + "//savegame");

            File fsoTemp = createFolder(fsoGame.getPath() + "//temp");
            fileLog = createFile(fsoTemp.getPath(), "temp.txt");

            if (!fileLog.canWrite()) {
                stringBuilder.append(String.format("%s Файл %s не может быть записан \r\n", LocalDateTime.now(), fileLog.getPath()));
                return false;
            }

            File fsoMain = createFolder(fsoScr.getPath() + "//main");
            createFile(fsoMain.getPath(), "Main.java");
            createFile(fsoMain.getPath(), "Utils.java");

            File fsoTest = createFolder(fsoScr.getPath() + "//test");


            File fsoDrawables = createFolder(fsoRes.getPath() + "//drawables");
            File fsoVectors = createFolder(fsoRes.getPath() + "//vectors");
            File fsoIcons = createFolder(fsoRes.getPath() + "//icons");

            writeToLogFile(stringBuilder, fileLog);

            createFile(fsoGame.getPath(), "gameIsInstalled");
            return true;

        } catch (Exception e) {
            if (fileLog == null) {
                System.out.println("fileLog have value = " + e.getMessage());
            } else {
                writeToLogFile(stringBuilder.append(e.getMessage()), fileLog);
            }
            return false;
        }
    }

    /**
     * Метод записывает в файл (журнал логирования) переданные сообщения в виде текст
     *
     * @param stringBuilder - блок текста для записи
     * @param fileLog       - файл в который необходимо выполнить запись
     * @return - максимальный из параметров
     */
    private void writeToLogFile(StringBuilder stringBuilder, File fileLog) {
        try (FileOutputStream fos = new FileOutputStream(fileLog, true)) {

            String log = stringBuilder.toString();
            byte[] bytes = log.getBytes();
            fos.write(bytes, 0, bytes.length);

        } catch (FileNotFoundException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
        } catch (IOException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
        }
    }

    /**
     * Метод для создания каталога по заданному пути
     *
     * @param path - путь для создания каталона
     * @return - возвращает объект созданного каталога
     */
    private File createFolder(String path) {
        File fso = new File(path);

        if (fso.exists()) {
            stringBuilder.append(String.format("%s Каталог %s существует по адресу %s \r\n", LocalDateTime.now(), fso.getName(), fso.getPath()));
        } else {
            fso.mkdir();
            stringBuilder.append(String.format("%s Каталог %s создан по адресу %s \r\n", LocalDateTime.now(), fso.getName(), fso.getPath()));
        }
        return fso;
    }

    /**
     * Метод создает файл
     *
     * @param pathFolder - пусть к каталогу, где должен быть создан файл
     * @param fileName   - имя файла для создания в каталоге
     * @return - возвращает объект созданного файла
     * @throws IOException - обрабатывает исключение в случае, если при попытке создания файла возникла ошибка
     */
    private File createFile(String pathFolder, String fileName) throws IOException {
        try {
            File file = new File(pathFolder + "//" + fileName);
            if (file.createNewFile()) {
                stringBuilder.append(String.format("%s Файл %s создан по адресу %s \r\n", LocalDateTime.now(), fileName, pathFolder));
            } else {
                if (file.exists()) {
                    stringBuilder.append(String.format("%s Файл %s уже существует по адресу %s \r\n", LocalDateTime.now(), fileName, pathFolder));
                } else {
                    stringBuilder.append(String.format("%s Файл %s не удалось создать по адресу %s \r\n", LocalDateTime.now(), fileName, pathFolder));
                }
            }
            return file;
        } catch (IOException e) {
            writeToLogFile(stringBuilder.append(String.format("%s " + e.getMessage() + "\r\n", LocalDateTime.now())), fileLog);
            return null;
        }
    }
}
