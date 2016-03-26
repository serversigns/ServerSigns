/*
 * This file is part of ServerSigns.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.czymm.serversigns.signs;

import java.util.Arrays;

public class PlayerInputOptions {

    String name;

    String question = "";
    String[] answerLabels = new String[0];
    String[] answerDescriptions = new String[0];

    public PlayerInputOptions(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public boolean isValidAnswerLabel(String label) {
        for (String str : answerLabels) {
            if (str.equalsIgnoreCase(label)) {
                return true;
            }
        }

        return false;
    }

    public String getAnswerLabel(int index) {
        if (answerLabels.length > index && index > -1) {
            return answerLabels[index];
        }
        return null;
    }

    public void setAnswerLabel(int index, String newValue) {
        if (answerLabels.length > index && index > -1) {
            answerLabels[index] = newValue;
        }
    }

    public int getAnswersLength() {
        return answerLabels.length;
    }

    public String getAnswerDescription(int index) {
        if (answerDescriptions.length > index && index > -1) {
            return answerDescriptions[index];
        }
        return null;
    }

    public void setAnswerDescription(int index, String newValue) {
        if (answerDescriptions.length > index && index > -1) {
            answerDescriptions[index] = newValue;
        }
    }

    public void addAnswer(String label, String description) {
        answerLabels = Arrays.copyOf(answerLabels, answerLabels.length + 1);
        answerDescriptions = Arrays.copyOf(answerDescriptions, answerDescriptions.length + 1);

        answerLabels[answerLabels.length - 1] = label;
        answerDescriptions[answerDescriptions.length - 1] = description;
    }
}
