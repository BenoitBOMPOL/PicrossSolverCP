# -------------------------------------------------- #
# Picross Solver | Constraint Programming | Choco 🍫 #
# -------------------------------------------------- #

CHOCOJAR = /home/benoit/choco-parsers-4.10.14-light.jar

JAVAC = javac
JAVA = java
JOPT = -classpath $(CHOCOJAR)

SRCDIR = src

all:
	@clear
	@echo 🍫 Picross Solver 🍫
	@echo
	@echo Here are the following available commands
	@echo -e '\t'1. make buildsolver
	@echo -e '\t\t' Build necessary files for the solver.'\n'
	@echo -e '\t'2. make solve GRID=\"picross/godzilla.px\"
	@echo -e '\t\t' Solve the grid located at \"picross/godzilla.px\"'\n'
	@echo -e '\t'3. make benchmark
	@echo -e '\t\t' Solve every grid in the picross folder'\n'
	@echo -e '\t'4. make clean
	@echo -e '\t\t' Remove .class files

buildsolver:
	@$(JAVAC) $(JOPT) $(SRCDIR)/picross.java
	@$(JAVAC) $(JOPT) $(SRCDIR)/picrossSlv.java $(SRCDIR)/picross.java

solve: buildsolver
	@$(JAVA) -Xmx1024M -Xms1024M $(JOPT) -cp $(CHOCOJAR):$(SRCDIR) picrossSlv $(GRID)

benchmark: buildsolver
	@clear; for grid in picross/*.px; do echo Running solver on file "$$grid"...; $(JAVA) $(JOPT) -cp $(CHOCOJAR):$(SRCDIR) picrossSlv "$$grid"; echo; done

clean:
	@rm -f $(SRCDIR)/*.class

.PHONY: default clean