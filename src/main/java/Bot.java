import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Bot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try{
            telegramBotsApi.registerBot(new Bot());
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }
    }
public void sendMsg(Message message, String text){
    SendMessage sendMessage = new SendMessage();
    sendMessage.enableMarkdown(true);
    sendMessage.setChatId(message.getChatId().toString());
    sendMessage.setText(text);
    try{
        sendMessage(sendMessage);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }

}
    public HashMap<Long, Game> games = new HashMap<Long, Game>();
    public ArrayList<String> tasks= filereader("file_with_tasks.txt");
    public ArrayList<String> questions = filereader("file_with_questions.txt");
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        if(games==null){
            greating(message);
        }
        else {
            if(games.containsKey(message.getChatId())){
                if(games.get(message.getChatId()).game_status){
                    switch (message.getText()){
                        case "/truth":
                            int rand1 = new Random().nextInt(questions.size()-1);
                            sendMsg(message,questions.get(rand1));
                            sendMsg(message,"Нажмите /next\nЧтобы продолжить игру");
                            break;
                        case "/todo":
                            int rand2 = new Random().nextInt(tasks.size()-1);
                            sendMsg(message,tasks.get(rand2));
                            sendMsg(message,"Нажмите /next\nЧтобы продолжить игру");
                            break;
                        case "/next":
                            int rnd = new Random().nextInt(games.get(message.getChatId()).players.size()-1);
                            String name = games.get(message.getChatId()).players.get(rnd);
                            sendMsg(message, name + ", твой ход!\nПравда или действие?\nЧтобы покаинуть игру, нажмите /endgame");
                            sendMsg(message,"/truth\n/todo");
                            break;
                        case "/endgame":
                            games.get(message.getChatId()).clear();
                            sendMsg(message,"Конец игры.");
                            break;
                        default:
                            sendMsg(message,"Вы должны выбрать\n/truth или /todo\nЕсли вы хотите закончить игру нажмите /endgame ;(");
                            break;
                    }
                }
                else{
                    if(games.get(message.getChatId()).recruit){
                        switch (message.getText()){
                            case "/help":
                                sendMsg(message,"Чтобы выйти из игры нажмите /stop\nЧтобы завершить набор игроков нажмите /finish");
                                break;
                            case "/stop":
                                games.get(message.getChatId()).clear();
                                sendMsg(message,"Игра отменена...");
                                break;
                            case "/finish":
                                games.get(message.getChatId()).recruit = false;
                                games.get(message.getChatId()).game_status = true;
                                sendMsg(message,"Игра началась!");
                                int rnd = new Random().nextInt(games.get(message.getChatId()).players.size()-1);
                                String name = games.get(message.getChatId()).players.get(rnd);
                                sendMsg(message, name + ", твой ход!\nПравда или действие?");
                                sendMsg(message,"/truth\n/todo");
                                break;
                            default:
                                String n_name = message.getText();
                                games.get(message.getChatId()).players.add(n_name);
                                sendMsg(message, "Игрок " + message.getText() + " добавлен!\nНапишите имя другого игрока!");
                                sendMsg(message, "Если вы хотите завершить добавление игроков нажмите /finish\nЕсли вы хотите отменить набор, нажмите /stop");
                                break;
                        }
                    }
                    else{
                        switch (message.getText()){
                            case "/start": {
                                sendMsg(message, "Приветствую вас в игре Правда или Действие!\nДля начала игры нажмите /newgame");
                            }
                            break;
                            case "/newgame":
                                games.get(message.getChatId()).clear();
                                sendMsg(message, "Для начала, нужно добавить новых игроков!\nВведите имя игрока,\nНапример: \"Алиса\"");
                                Game game = new Game();
                                game.recruit = true;
                                game.game_status = false;
                                games.put(message.getChatId(), game);
                                break;
                            case "/help":
                                sendMsg(message, "Чтобы начать игру напишите /newgame");
                                break;
                            case "/endgame":
                                sendMsg(message, "Вы еще не начали игру! Чтобы начать игру, нажмите /newgame !");
                            default:
                                sendMsg(message,"Для начала игры нажмите /newgame");
                        }
                    }
                }
            }
            else{
                greating(message);
            }
        }
    }

    public void greating(Message message) {
        switch (message.getText()) {
                case "/start": {
                    sendMsg(message, "Приветствую вас в игре Правда или Действие!\nДля начала игры нажмите /newgame");
                }
                break;
                case "/newgame":
                    sendMsg(message, "Для начала, нужно добавить новых игроков!\nВведите имя игрока,\nНапример: \"Алиса\"");
                    Game game = new Game();
                    game.recruit = true;
                    game.game_status = false;
                    games.put(message.getChatId(), game);
                    break;
                case "/help":
                    sendMsg(message, "Чтобы начать игру напишите /newgame");
                    break;
                case "/endgame":
                    games.get(message.getChatId()).clear();
                    sendMsg(message, "Игра завершена!");
                default:
                    sendMsg(message, "Чтобы начать игру напишите /newgame");
                    break;
            }
    }
    public ArrayList<String> filereader(String fname) {
        BufferedReader reader;
        ArrayList<String> lines = new ArrayList<String>();
        try {
            reader = new BufferedReader(new FileReader(
                    "./src/main/resources/"+fname));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    public String getBotUsername() {
        return "AITU_TEAM_17_BOT";
    }

    public String getBotToken() {
        return "911544664:AAEdWx6v86DoYeahwsIAzcbo0b5wYActhV0";
    }
}
