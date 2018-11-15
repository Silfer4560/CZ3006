<?php if(!isset($_SESSION)) { session_start(); } ?>
<!DOCTYPE html>
<html lang="en">
<style>
table.minimalistBlack {
  border: 3px solid #000000;
  width: 15%;
  text-align: left;
  border-collapse: collapse;
}
table.minimalistBlack td, table.minimalistBlack th {
  border: 1px solid #000000;
  padding: 5px 4px;
}
table.minimalistBlack tbody td {
  font-size: 13px;
}
table.minimalistBlack thead {
  background: #CFCFCF;
  background: -moz-linear-gradient(top, #dbdbdb 0%, #d3d3d3 66%, #CFCFCF 100%);
  background: -webkit-linear-gradient(top, #dbdbdb 0%, #d3d3d3 66%, #CFCFCF 100%);
  background: linear-gradient(to bottom, #dbdbdb 0%, #d3d3d3 66%, #CFCFCF 100%);
  border-bottom: 3px solid #000000;
}
table.minimalistBlack thead th {
  font-size: 15px;
  font-weight: bold;
  color: #000000;
  text-align: left;
}
table.minimalistBlack tfoot {
  font-size: 14px;
  font-weight: bold;
  color: #000000;
  border-top: 3px solid #000000;
}
table.minimalistBlack tfoot td {
  font-size: 14px;
}
</style>
<head>
	<meta charset="utf-8">
	<title>CZ3006 Assignment 2</title>
</head>
<body>
	<?php if(isset($_POST) && (isset($_POST['submit']) && $_POST['submit'] == "Submit"))?>
	<?php
		//check for order submission
		$name = $_POST["username"];
		$paymentMethod = $_POST["Payment"];
		$apples = $_POST["applesName"];
		$bananas =$_POST["bananasName"];
		$oranges =$_POST["orangesName"];
		$totalCost = $_POST["totalCostName"];
		// If any of the quantities are blank, set them to zero
		if ($apples == "") {
			$apples = 0;
		}
		if ($bananas == "") {
			$bananas = 0;
		}
		if ($oranges == "") {
			$oranges = 0;
		}
		// fruits ordered
		$apples_ordered = (int)$apples;
		$bananas_ordered = (int)$bananas;
		$oranges_ordered = (int)$oranges;
		//compute cost
		$apples_price = (0.69*(int)$apples);
		$bananas_price = (0.39*(int)$bananas);
		$oranges_price = (0.59*(int)$oranges);
		$totalCost = (0.69 * (int)$apples)+(0.39*(int)$bananas)+(0.59*(int)$oranges);
		//read from file using r
		//use xy coordinates to find number
		$orderFile = fopen("order.txt", "r") or die("Unable to open file for read!");
		$apples = substr(fgets($orderFile), 24, -1);
		$oranges = substr(fgets($orderFile), 25, -1);
		$bananas = substr(fgets($orderFile), 25, -1);
		fclose($orderFile);
		//write to file using w
		$orderFile = fopen("order.txt", "w") or die("Unable to open file for write!");
		$order = "Total number of apples: " . ((int)$apples+$apples_ordered) . "\r\nTotal number of oranges: " .((int)$oranges+$oranges_ordered) . "\r\nTotal number of bananas: " . ((int)$bananas+$bananas_ordered) . "\r\n";
		fwrite($orderFile, $order);
		fclose($orderFile);
	?>
	<legend>Receipt</legend>
<label>Name: <?php echo $name;?></label><br>
	<table class = "minimalistBlack">
		<tbody>
			<tr>
				<td>Fruit</td>
				<td>Quantity</td>
				<td>Price</td>
			</tr>
			<tr>
				<td>Apples</td>
				<td><?php echo $apples_ordered; ?></td>
				<td><?php echo $apples_price?></td>
			</tr>
			<tr>
				<td>Bananas</td>
				<td><?php echo $bananas_ordered;?></td>
				<td><?php echo $bananas_price;?></td>
			</tr>
			<tr>
				<td>Oranges</td>
				<td><?php echo $oranges_ordered; ?></td>
				<td><?php echo $oranges_price; ?></td>
			</tr>
		</tbody>
	</table>
	<br>
	<label>Total Price: <?php echo $totalCost;?></label><br>
	<label for="Username">Your username is: <?php echo $name; ?></label><br>
	<label for="PaymentMethod">Your chosen method of payment is: <?php echo $paymentMethod;?></label><br>
	</body>
</html>
