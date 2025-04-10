package ru.zzz3230.tetris.model.gameplay;

import ru.zzz3230.tetris.dto.BlockData;
import ru.zzz3230.tetris.dto.GameRulesData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockOrderGenerator {
    record NumberWeight(int number, float weight) {}

    private final GameRulesData gameRulesData;
    private Map<Integer, Integer> frequencies;

    public BlockOrderGenerator(GameRulesData gameRulesData){
        this.gameRulesData = gameRulesData;

        frequencies = new HashMap<>();
        for (int i = 0; i < gameRulesData.getAvailableBlocks().size(); i++) {
            frequencies.put(i, 0);
        }
    }

    private int nextBlockIndex(){
        List<NumberWeight> weightedNumbers = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
            int number = entry.getKey();
            int frequency = entry.getValue();
            float weight = 1.0f / (frequency + 1); // +1 чтобы избежать деления на 0

            // Увеличиваем вес для периодических чисел
            if(gameRulesData.getAvailableBlocks().get(number).periodicRate() != 0){
                weight *= gameRulesData.getAvailableBlocks().get(number).periodicRate();
            }
            
            weightedNumbers.add(new NumberWeight(number, weight));
        }

        double totalWeight = weightedNumbers.stream()
                .mapToDouble(nw -> nw.weight)
                .sum();

        // Генерируем случайное число в диапазоне [0, totalWeight)
        double randomValue = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;

        // Выбираем число на основе сгенерированного значения
        for (NumberWeight nw : weightedNumbers) {
            cumulativeWeight += nw.weight;
            if (randomValue <= cumulativeWeight) {
                return nw.number;
            }
        }

        // Если что-то пошло не так (например, из-за округления), вернуть последнее число
        return weightedNumbers.getLast().number;
    }

    public BlockData nextBlock() {
        int id = nextBlockIndex();
        frequencies.put(id, frequencies.get(id) + 1);
        return gameRulesData.getAvailableBlocks().get(id);
    }
}
