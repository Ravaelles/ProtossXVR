======================
===== ProtossXVR =====
== StarCraft AI Bot ==
======================

   This is AI for StarCraft: Brood War. It has been created in Java, using JNIBWAPI in version 0.3.7, using BWAPI 4.0.1.

   To run it follow instructions at: http://sscaitournament.com/index.php?action=tutorial
There's an entire Eclipse project inside. It should be easy to run if you follow the instructions. To inject this code you'll need ChaosLauncher and you'll find all instructions necessary in the tutorial above. Once you follow provided (and very good) readme file (in the JNIBWAPI Starter Pack) it shoudln't be hard. But you must have at leats minimal experience with Eclipse.


What AI does. Well, basically all it can to win. 
Which is:
   + Focuses on land units like Zealots, Dark Templars and Dragoons
   + Establishes new bases as early as it makes sense
   + Strategy is offensive, with decisive pushes into new enemy bases
   + Efficiency depends on Dark Templars usage to harass the enemy
   + AI tries to avoid deaths of wounded units, but it is offensive by default
   + When units are overwhelmed they won't try to attack unless it's crucial
   + Usage of spells like Psionic Storm or Stasis Field, but rarely
   + It will build Reavers as well
   + Countless small behaviors like avoiding to build in the middle of choke point
