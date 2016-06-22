package edu.brown.cs.atari_vision.caffe.training;

import burlap.behavior.policy.GreedyQPolicy;
import burlap.behavior.policy.Policy;
import burlap.behavior.policy.RandomPolicy;
import burlap.behavior.singleagent.Episode;
import burlap.behavior.singleagent.auxiliary.StateReachability;
import burlap.behavior.singleagent.auxiliary.valuefunctionvis.ValueFunctionVisualizerGUI;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.Environment;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;
import edu.brown.cs.atari_vision.ale.burlap.action.ActionSet;
import edu.brown.cs.atari_vision.caffe.exampledomains.NNGridWorld;
import edu.brown.cs.atari_vision.caffe.learners.DeepQLearner;
import edu.brown.cs.atari_vision.caffe.vfa.NNVFA;
import org.bytedeco.javacpp.FloatPointer;

import static org.bytedeco.javacpp.caffe.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by MelRod on 5/28/16.
 */
public class TrainingHelper {

    DeepQLearner learner;
    NNVFA vfa;
    Policy testPolicy;
    Environment trainingEnv;
    Environment testEnv;
    ActionSet actionSet;

    int maxEpisodeFrames = 10000;
    int totalTrainingFrames = 10000000;

    int testInterval = 100000;
    int numTestEpisodes = 10;


    List<State> sampleStates;
    int numSampleStates = -1;

    public TrainingHelper(DeepQLearner learner, NNVFA vfa, Policy testPolicy, ActionSet actionSet, Environment trainingEnv, Environment testEnv) {
        this.learner = learner;
        this.vfa = vfa;
        this.testPolicy = testPolicy;
        this.trainingEnv = trainingEnv;
        this.testEnv = testEnv;
        this.actionSet = actionSet;
    }

    public void setNumSampleStates(int n) {
        numSampleStates = n;
    }

    public void setTotalTrainingFrames(int n) {
        totalTrainingFrames = n;
    }

    public void setNumTestEpisodes(int n) {
        numTestEpisodes = n;
    }

    public void setTestInterval(int i) {
        testInterval = i;
    }

    public void run() {
        if (numSampleStates > 0) {
            System.out.println("Sampling random states");

            // Take a random sample of states
            Policy randomPolicy = new RandomPolicy(actionSet.actionTypeList());
            sampleStates = new ArrayList<>(numSampleStates);

            while (sampleStates.size() < numSampleStates) {
                // Run a random episode
                Episode ea = runEpisode(randomPolicy, testEnv);
                int episodeSize = ea.numTimeSteps();

                // Random sample of unique states from the episode
                Random rng = new Random();
                int numStatesToAdd = Math.min(episodeSize, numSampleStates - sampleStates.size());
                for (int i = 0; i < numStatesToAdd; i++) {
                    sampleStates.add(ea.state(rng.nextInt(episodeSize)));
                }
            }

            querySampleQs();
        }

        int frameCounter = 0;
        int episode = 0;

        int testCountDown = testInterval;

        while (frameCounter < totalTrainingFrames) {
            System.out.println(String.format("Training Episode %d at frame %d", episode, frameCounter));

            trainingEnv.resetEnvironment();
            Episode ea = learner.runLearningEpisode(trainingEnv, Math.min(totalTrainingFrames - frameCounter, maxEpisodeFrames));
            double totalReward = 0;
            for (double r : ea.rewardSequence) {
                totalReward += r;
            }
            System.out.println(String.format("Episode reward: %.2f", totalReward));
            System.out.println();

            testCountDown -= ea.numTimeSteps();
            if (testCountDown <= 0) {
                runTestSet();
                testCountDown += testInterval;
            }

            frameCounter += ea.numTimeSteps();
            episode++;
        }

        System.out.println("Done Training!");
    }

    public void querySampleQs() {
        double totalMaxQ = 0;
        for (State state : sampleStates) {
            FloatBlob qVals = vfa.qValuesForState(state);
            totalMaxQ += vfa.blobMax(qVals, 0);
        }

        double averageMaxQ = totalMaxQ/numSampleStates;
        System.out.println(String.format("Average Max Q-Value for sample states: %.3f", averageMaxQ));
    }

    public void runTestSet() {

        // Test the MaxQValues of the sample states
        if (sampleStates != null) {
            querySampleQs();
        }


        // Run the test policy on test episodes
        System.out.println("Running Test Set...");
        double totalTestReward = 0;
        for (int e = 1; e <= numTestEpisodes; e++) {
            Episode ea = runEpisode(testPolicy, testEnv);

            double totalReward = 0;
            for (double reward : ea.rewardSequence) {
                totalReward += reward;
            }

            System.out.println(String.format("%d: Reward = %.2f", e, totalReward));
            totalTestReward += totalReward;
        }

        System.out.println(String.format("Average Test Reward: %.2f", totalTestReward/numTestEpisodes));
        System.out.println();
    }

    public Episode runEpisode(Policy policy, Environment env) {
        env.resetEnvironment();
        Episode ea = new Episode();

        int eFrameCounter = 0;
        while(!env.isInTerminalState() && (eFrameCounter < maxEpisodeFrames || maxEpisodeFrames == -1)){
            State curState = env.currentObservation();
            Action action = policy.action(curState);

            EnvironmentOutcome eo = env.executeAction(action);
            ea.transition(eo.a, eo.op, eo.r);

            eFrameCounter++;
        }

        return ea;
    }
}