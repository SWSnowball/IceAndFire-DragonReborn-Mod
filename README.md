Mod Features (up to 2026.2.21): 
==========
1. Improvements to Dragon-Training Item Functions
1. Dragon Horn:
① Added more floating text display
② Allows storing dead dragons
2. Dragon Flute:
① Added stealth mode change (Clear Aggression / Landing Mode, sneak and click right mouse to switch the mode)
3. Sleep Dragon Staff (new item):
① Sets whether the dragon can sleep
4. Summoning Crystal:
① Added function to track dragon's position in real-time and display the information in the tooltip
5. Dragon Body Amulet(Protector) / Release Tool:
① The amulet can prevent other players from looting the dragon's body upon death (If you are the owner of the protected dragon, your looting action will be simply refused, with other players who tried to loot will take damages)
6. Dragon Adrenaline (available in regular and concentrated versions):
① When used on a dragon, revives it within a certain time (regular: revival time = dragon's age in days × 5s, concentrated: 4s); after revival, sets dragon's health to 20 points and applies Weakness III effect (regular: 5 minutes, concentrated: 10 minutes)
7. Dragon Dizziness Snowball (New Item)
① Throw this new snowball to a dragon can make it dizzy for 10 seconds (While the dragon playing dizziness animation, and can't move or fly. And...I added it to avoid you being chased by wild dragons, not added it for hunting dragons.)

2. Dragon Interaction Actions with Player
① Petting: Petting the dragon increases affection and mood, maintaining a continuous petting state. Different animations and text feedback appear when petting different parts.
② Hug (not started developing)
③ Feeding (original function): In the original version, right-click feeding had no animation. Reused the dragon's melee bite animation.

3. Mental Data
① Added three types: Affection, Positive Emotion Weight, and Loneliness, with a refined UI for display
② Added the dragon's occasional proactive requests for player interaction
③ Various interactions and events will affect these data
④ When the player is too far from the dragon (> 80 blocks), the dragon enters a lonely state, with text prompts at certain stages

4. Animation Related (all animations have multiple styles, randomly combined; random features not developed yet)
① Added dragon's petting feedback animations
② Added player's hand animations for petting the dragon (not started)
③ Dragon will look at the player when approached (Head and neck animation, activated when player to dragon's distance < 10 blocks)

5. Text Prompt System (in development)
① Literary-style text prompts appear when conditions or times are met, providing feedback (WARNING: only support Chinese texts; Too many repeated texts will display in the chat box that will make it too full, suggested to turn this function off; This function is still developing in progress)

6. Dragon Expansion Feedback System (not started)
① Once affection reaches a certain level, the dragon may drop gifts for the player during interactions (e.g., shed scales)

7. Configuration Files
① You can change some Mod features by adjusting the configuration file (dragonreborn-common.toml), included text system settings, dragon animation settings, etc. (WARNING: I don't suppose you to change the animation parameters, it affects complicated mathematics operations which helps to show the animation's appearances, if you want to, please research on the source code PetttingAnimationApplier.class .)
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/

Installation information
=======
Supported Minecraft Versions : 1.20.1

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
The MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

MDG Legacy:
==========
This template uses [ModDevGradle Legacy](https://github.com/neoforged/ModDevGradle). Documentation can be found [here](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md).
