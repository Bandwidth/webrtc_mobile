import UIKit

// This controller renders the list of callable users

class PersonTableViewController: UITableViewController {
    public var people = amplifyCaller.listPeople()
    
    
}

extension PersonTableViewController {
    
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
    
        return people.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "PersonCell", for: indexPath)
        
        
        let person = people[indexPath.row]
        cell.textLabel?.text = "\(person.firstName) \(person.lastName)"
        cell.detailTextLabel?.text = person.clientId
        cell.imageView?.image = UIImage(systemName: "photo")
        
        return cell
    }
    
    override func tableView(_ tableView : UITableView, titleForHeaderInSection section: Int) -> String? {
        return "List of Users"
    }
    
    
    // MARK: - Navigation

    // In a storyboard-based application, you will often want to do a little preparation before navigation
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        // Get the new view controller using segue.destination.
        // Pass the selected object to the new view controller.
        
        if segue.identifier == "ShowCallSegue",
           let destination = segue.destination as? ViewController,
           let cellIndex = tableView.indexPathForSelectedRow?.row
        {
//            let person = people[cellIndex]
            destination.callee = people[cellIndex]
        }
    }
    
}

