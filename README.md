# Plasmid
Plasmid is a library for creating server-side minigames with Fabric.
Plasmid does all the boring work relating to minigame implementation, to rather allow focus effort on just the game itself.

Plasmid is the core of the [Nucleoid Project](https://nucleoid.xyz/), an effort to build an open source ecosystem for server-side Minecraft minigames.
You can view many examples of games implemented with Plasmid on the [Nucleoid GitHub Organisation](https://github.com/NucleoidMC).
You may even be interested in playing some of them over on our testing Minecraft server at `nucleoid.xyz`! 
You can also find us on our [Discord](https://nucleoid.xyz/discord) if you have any troubles or queries, or would like to get involved.

## Using
This is a mirror of our [Getting Started](https://docs.nucleoid.xyz/plasmid/getting-started/) wiki page and provides a basic introduction to Plasmid concepts.
You can view our [wiki site](https://docs.nucleoid.xyz/plasmid/) for more detailed information.

If you would like to get up and running quickly with a basic game setup, clone the [plasmid-starter](https://github.com/NucleoidMC/plasmid-starter) repository, run `init.py`, and then delete `.git`, `README.md`, and `init.py`. Alternatively, if you are looking for examples of existing implemented games, take a look through the [Nucleoid Organisation](https://github.com/NucleoidMC) repositories.

### Adding to Gradle
Assuming you [already have a Fabric workspace set up](https://fabricmc.net/wiki/tutorial:setup), the first step to setting up Plasmid will be adding it to your gradle buildscript. You will need to add the maven repository as well as the plasmid dependency. `PLASMID_VERSION` should be replaced with the latest version from [Maven](https://maven.nucleoid.xyz/xyz/nucleoid/plasmid).

This tutorial is currently updated for **Plasmid 0.5.x**.

```gradle
repositories {
  maven { url = 'https://maven.nucleoid.xyz/' }
}

dependencies {
  // ...
  modImplementation 'xyz.nucleoid:plasmid:PLASMID_VERSION'
}
```

### Creating a game type
A "game type" (`GameType`) is the entry-point to creating a game with Plasmid: they provide a unique identifier for your game, as well as all the information needed for it to be able to call your code when the game starts.

Plasmid is designed to encourage data-driven games, and works with the concept of a "game config". A game config is essentially a specific variation of a game type! This may involve a different map to play on, or entirely different game mechanics. A game config is simply defined as a JSON file in a datapack that references your `GameType` and passes along any extra data that may be useful for configuring your game. While this may be a bit more work at first, it is very powerful in allowing games to be much easier to tweak or produce multiple variations of without duplicating code. More on configs later!

To register a `GameType`, you will need to call `GameType.register()` in your `ModInitializer` class. A call to register a `GameType` may look something like:
```java
GameType.register(
        new Identifier("plasmid_example", "example"),
        ExampleGameConfig.CODEC,
        ExampleGame::open
);
```

Let's break down what is going on here:
- `new Identifier("plasmid_example", "example")`
    - declares the unique identifier for this _game type_ that will be referenced by game config JSONs
- `ExampleGameConfig.CODEC`
    - a `Codec` that will be used to load the game configuration from a JSON file (more on this later!)
- `ExampleGame::open`
    - a method reference to a function that will be used to start your game when a player requests it

This naturally will not compile yet: neither `ExampleGame` nor `ExampleGameConfig` exist! Let's get to that.

### Creating our config in code
First we will create our `ExampleGameConfig` class, which will hold a `String` field that will be used as a message to send to the player when they join. Java's new [Records](https://docs.oracle.com/en/java/javase/16/language/records.html) are perfect for configs, but not required!

```java
public record ExampleGameConfig(String greeting) {
}
```

That's simple enough! But we're missing the `CODEC` field that we referenced earlier. What is that about?

A `Codec` is a very helpful tool implemented by Mojang's [DataFixerUpper](https://github.com/Mojang/DataFixerUpper) library that essentially allows for convenient serialization and deserialization of a Java object to a JSON file. A more detailed explanation of Codecs by Drullkus can be found [here](https://gist.github.com/Drullkus/1bca3f2d7f048b1fe03be97c28f87910), but for simple purposes, all you need to know is the pattern for putting them together.

Essentially, a Codec describes *how an object is serialized and deserialized*. Simply, they can be created from a list of fields and how those fields should be serialized. It goes like this:
```java
public record ExampleGameConfig(String greeting) {
    public static final Codec<ExampleGameConfig> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("greeting").forGetter(ExampleGameConfig::greeting)
        ).apply(instance, ExampleGameConfig::new);
    });
}
```

This will correspond to a JSON file that looks something like:
```json
{
  "greeting": "Hello World!"
}
```

Most things here you can ignore: you only really need to worry about what's in the `instance.group(...)` call, and the generic on the Codec. To look at each relevant part more specifically:
- `Codec<ExampleGameConfig>`
    - The type of class that is being deserialized into is passed as a generic parameter to the `Codec`.
- `Codec.STRING.fieldOf(...).forGetter(...)`
    - This adds a field with a given name and type that will be read from the JSON.
    - You will notice that `Codec.STRING` is itself a `Codec<String>`! Every field you declare will require a Codec to describe how that field should be handled. In this case, we're indicating that the greeting field should be loaded using `Codec.STRING`. In the same way, we could reference any other codec we create to add it as a field! This is very useful in allowing combinations of codecs to create complex structures!
        - Codec tip: most serializable Minecraft types will hold a static `CODEC` field for use (e.g. `BlockPos.CODEC` or `Identifier.CODEC`). If not, we bundle a `MoreCodecs` type which provides some common ones that are not included in the vanilla codebase (e.g. `MoreCodecs.TEXT`).
    - The parameter to `.fieldOf()` specifies the name of the field (in JSON) that this value will be read from.
    - `.forGetter()` specifies how the value of a field should be read back from our config object. This is useful since codecs allow for both serialization and deserialization, and the getter is required to turn the object back into data. We can use a method reference here since we're using a record.
- `ExampleGameConfig::new`
    - This tells the codec how to create the object once all the fields have been deserialized. This requires a method reference to the constructor for the given object with all the fields **in order as they were specified!**.
    - For example, if we passed `Codec.STRING.fieldOf("foo")` and then `Codec.INT.fieldOf("bar)`, the constructor would take a `(String, int)`.
    - But here we take in one `String` field, and the constructor we reference also takes a single `String` parameter.

The end result of all this Codec work is that when we create a game config, all this data will be automatically parsed from our JSON file and passed to our game code!

#### Creating a config
Now that we know what data our config should hold, we can create an actual game config JSON for Plasmid to load.

All game configs need to be located in your mod resources (or [datapack](https://github.com/NucleoidMC/game-configs)!) at `data/<namespace>/games/<id>.json`. For the purpose of a mod, the `namespace` should just be your mod id, and the `id` can be any unique name that will later be used to reference your game config from inside Minecraft.

Plasmid requires only 1 JSON field from the config, while the rest is loaded as per the config codec that you set up. There are however also some additional optional fields which may be useful to define. The only required field is the `type`, which refers to the `GameType` you created earlier in `namespace:path` format (e.g. in our case, `plasmid_example:example`).

For our purposes, our game config at `data/plasmid_example/games/hello_world_example.json` will look like:
```json
{
  "type": "plasmid_example:example",
  "greeting": "Hello, World!"
}
```

We can also add some additional builtin fields to our JSON such as a `name`, `short_name`, `description`, and `icon`.
This may look like:
```json5
{
  "type": "plasmid_example:example",
  "name": "Hello World Example!",
  "description": ["Look at my cool game!", "It greets you when you join."],
  "icon": "minecraft:apple"
  // ...
}
```

`name` and `description` can also reference translation keys due to being [JSON Text Components](https://minecraft.wiki/w/Raw_JSON_text_format). For example, this may instead be: `"name": {"translation": "game.plasmid_example.hello_world_example"}`.

#### A note on translations
Translations are a bit non-standard in Plasmid due to it being entirely server-side! Usually translations are stored with the game client, and the server simply sends over _translation keys_ which are then turned into relevant readable text on the client-side. Here, however, we need to instead handle translations by changing the packets that get sent to players such that they are correctly translated _before_ the client even receives it. This is a lot of work! Luckily, this is handled by [Server Translations](https://github.com/arthurbambou/Server-Translations), and we do not need to worry about it!

All this actually means for you is that your language files need to go in the `data` folder instead of the `assets` folder (e.g. `data/<namespace>/lang/en_us.json`).

There are some default language keys we should worry about if we're not manually defining a name: `gameType.<namespace>.<id>` and `game.<namespace>.<id>`. These keys are applied for game _types_ and game _configs_ respectively. When resolving the readable name for a game config, both the config translation and type translation will be tested, with the type as a fallback. This means only the game type translation is strictly necessary.

For example, we may define our `data/plasmid_example/lang/en_us.json` as:
```json
{
  "gameType.plasmid_example.example": "Plasmid Example!",
  "game.plasmid_example.hello_world_example": "Hello World Example!"
}
```

### Writing the code to start our game
Now that we have set up a config and have told Plasmid how to read from it, we can finally write the code to actually start our game.

For the purpose of this example, let's create an `ExampleGame` class. We will use this class to hold the state of the game as well as our `ExampleGameConfig` that got loaded. For now though, we just need to create this `open` function that we referenced to the `GameType`.

This should look like:
```java
public class ExampleGame {
    public static GameOpenProcedure open(GameOpenContext<ExampleGameConfig> context) {
        // get our config that got loaded by Plasmid
        ExampleGameConfig config = context.config();

        // create a very simple map with a stone block at (0; 64; 0)
        MapTemplate template = MapTemplate.createEmpty();
        template.setBlockState(new BlockPos(0, 64, 0), Blocks.STONE.getDefaultState());

        // create a chunk generator that will generate from this template that we just created
        TemplateChunkGenerator generator = new TemplateChunkGenerator(context.server(), template);

        // set up how the world that this minigame will take place in should be constructed
        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(generator)
                .setTimeOfDay(6000);

        return context.openWithWorld(worldConfig, (activity, world) -> {
            // to be implemented
        });
    }
}
```

There is a lot to unpack here, but it's not too complex if we break it down. Our `open` will be called whenever a player starts this game. The function takes a `GameOpenContext`, which holds the data from our JSON config (`context.config()`), and must return a `GameOpenProcedure`, which instructs Plasmid how it should continue to set up the game. It is worth nothing that this function is run asynchronously on the thread pool, so it is safe to run whatever slow code here before the game starts.

The `GameOpenProcedure` is created from the `GameOpenContext.openWithWorld` function, and takes in a `RuntimeWorldConfig` as well as a lambda that accepts a `GameActivity` and `ServerWorld`. A runtime world is a concept within Plasmid that represents the fully isolated and temporary world that the game takes place within. It is automatically deleted when the game finishes. When a player joins the game, their inventory will be cleared, and when they leave, it will be restored back to them. A game activity is a specific set of logic that is running within a game: this is what we will configure to change game behaviour. We can switch the activity within a game at any point.

The `RuntimeWorldConfig` describes how this world should be created. The most important thing to be configured within here is the chunk generator: this tells the game how the world should generate. It would be possible to, for example, pass the overworld chunk generator here, but for our purpose, we're creating an empty world with a single stone block. This is handled through the convenience `TemplateChunkGenerator`: this takes a `MapTemplate`, which is just a very basic world that contains some blocks! The generator then loads from that into the world itself.

Finally, we need to address what to do in the lambda with the `GameActivity` parameter. The code inside this lambda will run on the *main server thread*, and is used to run the actual game setup code. This mainly involves registering event listeners, or setting global rules.

Event tip: we make use of [Stimuli](https://github.com/NucleoidMC/stimuli) for handling many events in games, so any event from there can be used within Plasmid.

For example:
```java
return context.openWithWorld(worldConfig, (activity, world) -> {
    activity.deny(GameRuleType.FALL_DAMAGE);

    activity.listen(GamePlayerEvents.ADD, player -> {
        // a player has been added!
    });
});
```

This code will disable fall damage for all players, as well as registering an event listener that will be called whenever a player is added to this game.

However! Before we give functionality to our brilliant example game, we need to respond to the *player offer event listener*. This is called *before* any player joins the game, and is able to accept or reject that join request. Most critically, the listener defines how and where the player should be spawned into our game world.

An example offer listener may look like:
```java
activity.listen(GamePlayerEvents.OFFER, offer -> {
    ServerPlayerEntity player = offer.player();
    return offer.accept(world, new Vec3d(0.0, 64.0, 0.0))
            .and(() -> {
                player.changeGameMode(GameMode.ADVENTURE);
            });
});
```

That's a lot! Let's break it down:
- We register a listener for `GamePlayerEvents.OFFER` which takes an `offer` parameter.
- We get the player instance who is trying to join from the offer.
- We call `offer.accept(...)` to accept the player into the game.
    - We pass the accept function a *world* and a *position* for the player to be teleported to. The world was passed to us above by Plasmid!
- We then call `.and(...)` on the result of `.accept(...)` in order to attach some additional spawn logic to be run when the player joins. In this case, that is to set the player's game mode to adventure mode as they join.

Now that we have that set up, we can return to our player add listener: as of right now, we're not doing anything when it is called. We want it to send a greeting to the player when they join. Let's implement that:
```java
GameSpace gameSpace = activity.getGameSpace();
activity.listen(GamePlayerEvents.ADD, player -> {
    LiteralText message = new LiteralText(config.greeting);
    gameSpace.getPlayers().sendMessage(message);
});
```

So we've added logic to send a message within the listener, but what is a `GameSpace`? A `GameSpace` is a concept introduced by Plasmid which, as the name implies, represents the _space_ within which a game is occurring. For all our purposes, that space is just this one dimension that the game is playing within. The `GameSpace` is useful for us in that it keeps track of all the players within it, as well as the `ServerWorld` that the game is taking place within. Here, we access the `GameSpace` through `GameActivity.getGameSpace()`.

Working with players additionally goes through a different Plasmid API: a `PlayerSet`. A `PlayerSet` represents just a list of players, and it can be iterated over or queried, but additionally provides utilities for performing bulk operations over many players. For example, sending a message! Here, we use `PlayerSet.sendMessage()` to send our greeting to every player within the game.

Tada! 🎉 We have a working game! But before we test it, let's do some minor reorganization. With all these handlers and lambdas, our code inside `createOpenProcedure` is going to get quite lengthy very quickly! It would be nice if we can put all event listeners on our `ExampleGame` object instead.

Turns out, that works just fine, and we are left with our final `ExampleGame` setup:
```java
public final class ExampleGame {
    private final ExampleGameConfig config;
    private final GameSpace gameSpace;
    private final ServerWorld world;

    public ExampleGame(ExampleGameConfig config, GameSpace gameSpace, ServerWorld world) {
        this.config = config;
        this.gameSpace = gameSpace;
        this.world = world;
    }

    public static GameOpenProcedure open(GameOpenContext<ExampleGameConfig> context) {
        // get our config that got loaded by Plasmid
        ExampleGameConfig config = context.config();

        // create a very simple map with a stone block at (0; 64; 0)
        MapTemplate template = MapTemplate.createEmpty();
        template.setBlockState(new BlockPos(0, 64, 0), Blocks.STONE.getDefaultState());

        // create a chunk generator that will generate from this template that we just created
        TemplateChunkGenerator generator = new TemplateChunkGenerator(context.server(), template);

        // set up how the world that this minigame will take place in should be constructed
        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(generator)
                .setTimeOfDay(6000);

        return context.openWithWorld(worldConfig, (activity, world) -> {
            ExampleGame game = new ExampleGame(config, activity.getGameSpace(), world);

            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.listen(GamePlayerEvents.OFFER, game::onPlayerOffer);
            activity.listen(GamePlayerEvents.ADD, game::onPlayerAdd);
        });
    }

    private PlayerOfferResult onPlayerOffer(PlayerOffer offer) {
        ServerPlayerEntity player = offer.player();
        return offer.accept(this.world, new Vec3d(0.0, 64.0, 0.0))
                .and(() -> {
                    player.changeGameMode(GameMode.ADVENTURE);
                });
    }

    private void onPlayerAdd(ServerPlayerEntity player) {
        LiteralText message = new LiteralText(this.config.greeting);
        this.gameSpace.getPlayers().sendMessage(message);
    }
}
```

### Testing the game!
Once everything compiles, we can finally launch up Minecraft. If our `GameType` is all correctly set up and game config JSON in place, once opening a world, we should be able to start our game by running: `/game open <id>`. (Remember, this is referencing the name of the JSON file and not the GameType!)

So in our case: `/game open plasmid_example:hello_world_example`
...and we should be joined into our void world with a stone block with a lovely greeting!

Now, any other player can join us too by running `/game join` or clicking the link that shows up in chat.

That's it! 🎉 
