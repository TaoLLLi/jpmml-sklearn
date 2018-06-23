/*
 * Copyright (c) 2018 Villu Ruusmann
 *
 * This file is part of JPMML-SkLearn
 *
 * JPMML-SkLearn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPMML-SkLearn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with JPMML-SkLearn.  If not, see <http://www.gnu.org/licenses/>.
 */
package sklearn;

import java.util.List;

public class EstimatorUtil {

	private EstimatorUtil(){
	}

	static
	public int getNumberOfFeatures(HasEstimatorEnsemble<?> hasEstimatorEnsemble){
		List<? extends Estimator> estimators = hasEstimatorEnsemble.getEstimators();

		for(Estimator estimator : estimators){
			return estimator.getNumberOfFeatures();
		}

		return -1;
	}
}